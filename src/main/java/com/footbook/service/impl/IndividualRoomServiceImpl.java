package com.footbook.service.impl;

import com.footbook.domain.Branch;
import com.footbook.domain.IndividualRoom;
import com.footbook.domain.IndividualRoomParticipant;
import com.footbook.domain.User;
import com.footbook.dto.request.room.CreateIndividualRoomRequest;
import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.dto.response.room.IndividualRoomDetailResponse;
import com.footbook.dto.response.room.IndividualRoomResponse;
import com.footbook.repository.BranchRepository;
import com.footbook.repository.IndividualRoomParticipantRepository;
import com.footbook.repository.IndividualRoomRepository;
import com.footbook.repository.UserRepository;
import com.footbook.service.IndividualRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualRoomServiceImpl implements IndividualRoomService {
    private final IndividualRoomRepository roomRepository;
    private final IndividualRoomParticipantRepository participantRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional
    public IndividualRoomResponse createRoom(CreateIndividualRoomRequest request) {
        UUID currentUserId = getCurrentUserId();

        Branch branch = branchRepository.findByIdAndIsActiveTrue(request.branchId())
            .orElseThrow(() -> new NoSuchElementException("Branch not found or inactive"));

        LocalDate scheduledDate = parseDate(request.scheduledDate());
        LocalTime startTime = parseTime(request.startTime(), "Start time");
        LocalTime endTime = parseTime(request.endTime(), "End time");

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingDateTime = LocalDateTime.of(scheduledDate, startTime);
        if (bookingDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Cannot create a room in the past");
        }

        if (startTime.isBefore(branch.getOperatingHoursStart()) || endTime.isAfter(branch.getOperatingHoursEnd())) {
            throw new IllegalArgumentException(
                String.format("Booking time must be within branch operating hours (%s - %s)",
                    branch.getOperatingHoursStart().format(TIME_FORMATTER),
                    branch.getOperatingHoursEnd().format(TIME_FORMATTER))
            );
        }

        if (roomRepository.hasUserConflict(currentUserId, scheduledDate, startTime, endTime)) {
            throw new IllegalArgumentException("You have a conflicting booking at this time");
        }

        IndividualRoom room = IndividualRoom.builder()
            .branchId(request.branchId())
            .ownerId(currentUserId)
            .scheduledDate(scheduledDate)
            .startTime(startTime)
            .endTime(endTime)
            .totalSlots(request.totalSlots())
            .notes(request.notes())
            .status(IndividualRoom.RoomStatus.OPEN)
            .build();

        room = roomRepository.save(room);

        IndividualRoomParticipant ownerParticipant = IndividualRoomParticipant.builder()
            .roomId(room.getId())
            .userId(currentUserId)
            .joinedAt(LocalDateTime.now())
            .build();

        participantRepository.save(ownerParticipant);

        log.info("Created individual room {} by user {}", room.getId(), currentUserId);

        checkAndAutoCloseRoom(room.getId());

        return mapToResponse(room, branch, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IndividualRoomResponse> getAllRooms(UUID branchId, LocalDate startDate, LocalDate endDate, String statusStr, Pageable pageable) {
        IndividualRoom.RoomStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = IndividualRoom.RoomStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value. Must be OPEN, FULL, or CANCELLED");
            }
        }

        Page<IndividualRoom> rooms = roomRepository.findRoomsWithFilters(branchId, startDate, endDate, status, pageable);

        List<UUID> roomIds = rooms.getContent().stream().map(IndividualRoom::getId).toList();
        Map<UUID, Long> participantCounts = roomIds.isEmpty() ? new HashMap<>() :
            participantRepository.findByRoomIdIn(roomIds).stream()
                .collect(Collectors.groupingBy(IndividualRoomParticipant::getRoomId, Collectors.counting()));

        Set<UUID> branchIds = rooms.getContent().stream().map(IndividualRoom::getBranchId).collect(Collectors.toSet());
        Map<UUID, Branch> branches = branchRepository.findAllById(branchIds).stream()
            .collect(Collectors.toMap(Branch::getId, b -> b));

        return rooms.map(room -> {
            Long filledSlots = participantCounts.getOrDefault(room.getId(), 0L);
            Branch branch = branches.get(room.getBranchId());
            return mapToResponse(room, branch, filledSlots.intValue());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public IndividualRoomDetailResponse getRoomById(UUID id) {
        IndividualRoom room = roomRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + id));

        Branch branch = branchRepository.findById(room.getBranchId())
            .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        List<IndividualRoomParticipant> participants = participantRepository.findByRoomIdOrderByJoinedAtAsc(id);

        Set<UUID> userIds = participants.stream().map(IndividualRoomParticipant::getUserId).collect(Collectors.toSet());
        userIds.add(room.getOwnerId());
        Map<UUID, User> users = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        User owner = users.get(room.getOwnerId());
        List<IndividualRoomDetailResponse.ParticipantInfo> participantInfos = participants.stream()
            .map(p -> {
                User user = users.get(p.getUserId());
                return new IndividualRoomDetailResponse.ParticipantInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getProfilePictureUrl(),
                    p.getJoinedAt()
                );
            })
            .toList();

        return new IndividualRoomDetailResponse(
            room.getId(),
            mapBranchToResponse(branch),
            new IndividualRoomDetailResponse.ParticipantInfo(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getProfilePictureUrl(),
                null
            ),
            room.getScheduledDate().format(DATE_FORMATTER),
            room.getStartTime().format(TIME_FORMATTER),
            room.getEndTime().format(TIME_FORMATTER),
            room.getTotalSlots(),
            participants.size(),
            room.getTotalSlots() - participants.size(),
            room.getNotes(),
            room.getStatus().name(),
            participantInfos,
            room.getCreatedAt(),
            room.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void joinRoom(UUID roomId) {
        UUID currentUserId = getCurrentUserId();

        IndividualRoom room = roomRepository.findByIdAndStatusNot(roomId, IndividualRoom.RoomStatus.CANCELLED)
            .orElseThrow(() -> new NoSuchElementException("Room not found or cancelled"));

        if (room.getStatus() == IndividualRoom.RoomStatus.FULL) {
            throw new IllegalStateException("Room is already full");
        }

        if (participantRepository.existsByRoomIdAndUserId(roomId, currentUserId)) {
            throw new IllegalStateException("You have already joined this room");
        }

        if (roomRepository.hasUserConflict(currentUserId, room.getScheduledDate(), room.getStartTime(), room.getEndTime())) {
            throw new IllegalArgumentException("You have a conflicting booking at this time");
        }

        long currentParticipants = participantRepository.countByRoomId(roomId);
        if (currentParticipants >= room.getTotalSlots()) {
            throw new IllegalStateException("Room is already full");
        }

        IndividualRoomParticipant participant = IndividualRoomParticipant.builder()
            .roomId(roomId)
            .userId(currentUserId)
            .joinedAt(LocalDateTime.now())
            .build();

        participantRepository.save(participant);
        log.info("User {} joined room {}", currentUserId, roomId);

        checkAndAutoCloseRoom(roomId);
    }

    @Override
    @Transactional
    public void leaveRoom(UUID roomId) {
        UUID currentUserId = getCurrentUserId();

        IndividualRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found"));

        if (room.getOwnerId().equals(currentUserId)) {
            throw new IllegalStateException("Room owner cannot leave the room. Please cancel the room instead.");
        }

        if (!participantRepository.existsByRoomIdAndUserId(roomId, currentUserId)) {
            throw new NoSuchElementException("You are not a participant in this room");
        }

        participantRepository.deleteByRoomIdAndUserId(roomId, currentUserId);
        log.info("User {} left room {}", currentUserId, roomId);

        if (room.getStatus() == IndividualRoom.RoomStatus.FULL) {
            room.setStatus(IndividualRoom.RoomStatus.OPEN);
            roomRepository.save(room);
            log.info("Room {} reopened after participant left", roomId);
        }
    }

    @Override
    @Transactional
    public void cancelRoom(UUID roomId) {
        UUID currentUserId = getCurrentUserId();

        IndividualRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found"));

        if (!room.getOwnerId().equals(currentUserId)) {
            throw new IllegalStateException("Only the room owner can cancel the room");
        }

        room.setStatus(IndividualRoom.RoomStatus.CANCELLED);
        roomRepository.save(room);
        log.info("Room {} cancelled by owner {}", roomId, currentUserId);
    }

    private void checkAndAutoCloseRoom(UUID roomId) {
        IndividualRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found"));

        long currentParticipants = participantRepository.countByRoomId(roomId);

        if (currentParticipants >= room.getTotalSlots() && room.getStatus() == IndividualRoom.RoomStatus.OPEN) {
            room.setStatus(IndividualRoom.RoomStatus.FULL);
            roomRepository.save(room);
            log.info("Room {} auto-closed (full capacity)", roomId);
        }
    }

    private IndividualRoomResponse mapToResponse(IndividualRoom room, Branch branch, int filledSlots) {
        User owner = userRepository.findById(room.getOwnerId())
            .orElseThrow(() -> new NoSuchElementException("Owner not found"));

        return new IndividualRoomResponse(
            room.getId(),
            mapBranchToResponse(branch),
            new IndividualRoomResponse.ParticipantSummary(
                owner.getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getProfilePictureUrl()
            ),
            room.getScheduledDate().format(DATE_FORMATTER),
            room.getStartTime().format(TIME_FORMATTER),
            room.getEndTime().format(TIME_FORMATTER),
            room.getTotalSlots(),
            filledSlots,
            room.getTotalSlots() - filledSlots,
            room.getNotes(),
            room.getStatus().name(),
            room.getCreatedAt(),
            room.getUpdatedAt()
        );
    }

    private BranchResponse mapBranchToResponse(Branch branch) {
        return new BranchResponse(
            branch.getId(),
            branch.getName(),
            branch.getAddress(),
            branch.getGoogleMapsUrl(),
            branch.getOperatingHoursStart().format(TIME_FORMATTER),
            branch.getOperatingHoursEnd().format(TIME_FORMATTER),
            branch.getContactPhone(),
            branch.getContactEmail(),
            branch.getLatitude(),
            branch.getLongitude(),
            branch.getIsActive(),
            branch.getCreatedAt(),
            branch.getUpdatedAt()
        );
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format");
        }
    }

    private LocalTime parseTime(String timeStr, String fieldName) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " must be in HH:mm format");
        }
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("User not found"))
            .getId();
    }
}

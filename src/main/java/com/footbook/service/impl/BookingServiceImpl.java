package com.footbook.service.impl;

import com.footbook.domain.*;
import com.footbook.dto.response.booking.BookingResponse;
import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.repository.*;
import com.footbook.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.footbook.util.ErrorMessages.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final IndividualRoomRepository individualRoomRepository;
    private final IndividualRoomParticipantRepository participantRepository;
    private final TeamRoomRepository teamRoomRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final BranchRepository branchRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        List<BookingResponse> allBookings = new ArrayList<>();
        allBookings.addAll(getIndividualRoomBookings(null, null));
        allBookings.addAll(getTeamRoomBookings(null, null));

        allBookings.sort((a, b) -> {
            int dateCompare = b.scheduledDate().compareTo(a.scheduledDate());
            if (dateCompare != 0) return dateCompare;
            return b.startTime().compareTo(a.startTime());
        });

        return allBookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyUpcomingBookings() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<BookingResponse> allBookings = new ArrayList<>();
        allBookings.addAll(getIndividualRoomBookings(today, currentTime));
        allBookings.addAll(getTeamRoomBookings(today, currentTime));

        allBookings.sort(
            Comparator.comparing(BookingResponse::scheduledDate)
                .thenComparing(BookingResponse::startTime)
        );

        return allBookings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyPastBookings() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        UUID currentUserId = getCurrentUserId();

        List<IndividualRoomParticipant> pastParticipations =
            participantRepository.findPastParticipationsByUser(currentUserId, today, currentTime);

        List<UUID> roomIds = pastParticipations.stream()
            .map(IndividualRoomParticipant::getRoomId)
            .toList();

        List<BookingResponse> bookings = new ArrayList<>();

        if (!roomIds.isEmpty()) {
            List<IndividualRoom> rooms = individualRoomRepository.findAllById(roomIds);
            bookings.addAll(mapIndividualRoomsToBookings(rooms));
        }

        List<UUID> userTeamIds = teamMemberRepository.findTeamIdsByUserId(currentUserId);
        if (!userTeamIds.isEmpty()) {
            List<TeamRoom> pastTeamRooms = teamRoomRepository.findAll().stream()
                .filter(r -> (userTeamIds.contains(r.getCreatorTeamId()) ||
                    (r.getOpponentTeamId() != null && userTeamIds.contains(r.getOpponentTeamId()))))
                .filter(r -> r.getStatus() != TeamRoom.TeamRoomStatus.CANCELLED)
                .filter(r -> r.getScheduledDate().isBefore(today) ||
                    (r.getScheduledDate().equals(today) && r.getStartTime().isBefore(currentTime)))
                .toList();

            bookings.addAll(mapTeamRoomsToBookings(pastTeamRooms));
        }

        bookings.sort((a, b) -> {
            int dateCompare = b.scheduledDate().compareTo(a.scheduledDate());
            if (dateCompare != 0) return dateCompare;
            return b.startTime().compareTo(a.startTime());
        });

        return bookings;
    }

    private List<BookingResponse> getIndividualRoomBookings(LocalDate today, LocalTime currentTime) {
        UUID currentUserId = getCurrentUserId();

        List<IndividualRoomParticipant> participations;
        if (today != null && currentTime != null) {
            participations = participantRepository.findUpcomingParticipationsByUser(currentUserId, today, currentTime);
        } else {
            participations = participantRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(currentUserId))
                .toList();
        }

        if (participations.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> roomIds = participations.stream()
            .map(IndividualRoomParticipant::getRoomId)
            .toList();

        List<IndividualRoom> rooms = individualRoomRepository.findAllById(roomIds);
        return mapIndividualRoomsToBookings(rooms);
    }

    private List<BookingResponse> getTeamRoomBookings(LocalDate today, LocalTime currentTime) {
        UUID currentUserId = getCurrentUserId();

        List<UUID> userTeamIds = teamMemberRepository.findTeamIdsByUserId(currentUserId);

        if (userTeamIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<TeamRoom> teamRooms;
        if (today != null && currentTime != null) {
            teamRooms = userTeamIds.stream()
                .flatMap(teamId -> teamRoomRepository.findUpcomingRoomsByTeam(teamId, today, currentTime).stream())
                .distinct()
                .toList();
        } else {
            teamRooms = teamRoomRepository.findAll().stream()
                .filter(r -> userTeamIds.contains(r.getCreatorTeamId()) ||
                    (r.getOpponentTeamId() != null && userTeamIds.contains(r.getOpponentTeamId())))
                .filter(r -> r.getStatus() != TeamRoom.TeamRoomStatus.CANCELLED)
                .toList();
        }

        return mapTeamRoomsToBookings(teamRooms);
    }

    private List<BookingResponse> mapIndividualRoomsToBookings(List<IndividualRoom> rooms) {
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> branchIds = rooms.stream().map(IndividualRoom::getBranchId).collect(Collectors.toSet());
        Map<UUID, Branch> branches = branchRepository.findAllById(branchIds).stream()
            .collect(Collectors.toMap(Branch::getId, b -> b));

        Set<UUID> ownerIds = rooms.stream().map(IndividualRoom::getOwnerId).collect(Collectors.toSet());
        Map<UUID, User> owners = userRepository.findAllById(ownerIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        List<UUID> roomIds = rooms.stream().map(IndividualRoom::getId).toList();
        Map<UUID, Long> participantCounts = participantRepository.findByRoomIdIn(roomIds).stream()
            .collect(Collectors.groupingBy(IndividualRoomParticipant::getRoomId, Collectors.counting()));

        return rooms.stream()
            .map(room -> {
                Branch branch = branches.get(room.getBranchId());
                User owner = owners.get(room.getOwnerId());
                Long filledSlots = participantCounts.getOrDefault(room.getId(), 0L);

                return new BookingResponse(
                    room.getId(),
                    "INDIVIDUAL",
                    mapBranchToResponse(branch),
                    room.getScheduledDate().format(DATE_FORMATTER),
                    room.getStartTime().format(TIME_FORMATTER),
                    room.getEndTime().format(TIME_FORMATTER),
                    new BookingResponse.BookingDetails(
                        room.getTotalSlots(),
                        filledSlots.intValue(),
                        owner.getFirstName() + " " + owner.getLastName(),
                        null,
                        null,
                        null
                    ),
                    room.getStatus().name(),
                    room.getCreatedAt()
                );
            })
            .toList();
    }

    private List<BookingResponse> mapTeamRoomsToBookings(List<TeamRoom> rooms) {
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> branchIds = rooms.stream().map(TeamRoom::getBranchId).collect(Collectors.toSet());
        Map<UUID, Branch> branches = branchRepository.findAllById(branchIds).stream()
            .collect(Collectors.toMap(Branch::getId, b -> b));

        Set<UUID> teamIds = new HashSet<>();
        rooms.forEach(r -> {
            teamIds.add(r.getCreatorTeamId());
            if (r.getOpponentTeamId() != null) {
                teamIds.add(r.getOpponentTeamId());
            }
        });
        Map<UUID, Team> teams = teamRepository.findAllById(teamIds).stream()
            .collect(Collectors.toMap(Team::getId, t -> t));

        return rooms.stream()
            .map(room -> {
                Branch branch = branches.get(room.getBranchId());
                Team creatorTeam = teams.get(room.getCreatorTeamId());
                Team opponentTeam = room.getOpponentTeamId() != null ? teams.get(room.getOpponentTeamId()) : null;

                return new BookingResponse(
                    room.getId(),
                    "TEAM",
                    mapBranchToResponse(branch),
                    room.getScheduledDate().format(DATE_FORMATTER),
                    room.getStartTime().format(TIME_FORMATTER),
                    room.getEndTime().format(TIME_FORMATTER),
                    new BookingResponse.BookingDetails(
                        null,
                        null,
                        null,
                        creatorTeam.getName(),
                        opponentTeam != null ? opponentTeam.getName() : "Waiting for opponent",
                        room.getRequiredTeamSize()
                    ),
                    room.getStatus().name(),
                    room.getCreatedAt()
                );
            })
            .toList();
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

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND))
            .getId();
    }
}

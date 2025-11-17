package com.footbook.service.impl;

import com.footbook.domain.Branch;
import com.footbook.domain.Team;
import com.footbook.domain.TeamMember;
import com.footbook.domain.TeamRoom;
import com.footbook.dto.request.room.CreateTeamRoomRequest;
import com.footbook.dto.request.room.JoinTeamRoomRequest;
import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.dto.response.room.TeamRoomDetailResponse;
import com.footbook.dto.response.room.TeamRoomResponse;
import com.footbook.dto.response.team.TeamDetailResponse;
import com.footbook.repository.*;
import com.footbook.service.TeamRoomService;
import com.footbook.service.TeamService;
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
public class TeamRoomServiceImpl implements TeamRoomService {
    private final TeamRoomRepository teamRoomRepository;
    private final BranchRepository branchRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamService teamService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional
    public TeamRoomResponse createRoom(CreateTeamRoomRequest request) {
        UUID currentUserId = getCurrentUserId();

        Branch branch = branchRepository.findByIdAndIsActiveTrue(request.branchId())
            .orElseThrow(() -> new NoSuchElementException("Branch not found or inactive"));

        Team team = teamRepository.findByIdAndStatus(request.teamId(), Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException("Team not found or disbanded"));

        if (!team.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException("Only the team captain can create team rooms");
        }

        long memberCount = teamMemberRepository.countByTeamId(request.teamId());
        if (memberCount < team.getRosterSize()) {
            throw new IllegalStateException("Team must have full roster (" + team.getRosterSize() + " members) to create a team room");
        }

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

        if (teamRoomRepository.hasTeamConflict(request.teamId(), scheduledDate, startTime, endTime)) {
            throw new IllegalArgumentException("Your team has a conflicting team room booking at this time");
        }

        List<UUID> teamMemberIds = teamMemberRepository.findUserIdsByTeamId(request.teamId());
        if (teamRoomRepository.hasTeamMembersIndividualConflict(teamMemberIds, scheduledDate, startTime, endTime)) {
            throw new IllegalArgumentException("One or more team members have conflicting individual room bookings at this time");
        }

        TeamRoom room = TeamRoom.builder()
            .branchId(request.branchId())
            .creatorTeamId(request.teamId())
            .scheduledDate(scheduledDate)
            .startTime(startTime)
            .endTime(endTime)
            .requiredTeamSize(team.getRosterSize())
            .status(TeamRoom.TeamRoomStatus.OPEN)
            .build();

        room = teamRoomRepository.save(room);
        log.info("Created team room {} by team {} (captain: {})", room.getId(), request.teamId(), currentUserId);

        return mapToResponse(room, branch, team, null, (int) memberCount, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamRoomResponse> getAllRooms(UUID branchId, LocalDate startDate, LocalDate endDate, Integer teamSize, String statusStr, Pageable pageable) {
        TeamRoom.TeamRoomStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = TeamRoom.TeamRoomStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value. Must be OPEN, MATCHED, or CANCELLED");
            }
        }

        Page<TeamRoom> rooms = teamRoomRepository.findRoomsWithFilters(branchId, startDate, endDate, teamSize, status, pageable);

        Set<UUID> branchIds = rooms.getContent().stream().map(TeamRoom::getBranchId).collect(Collectors.toSet());
        Map<UUID, Branch> branches = branchRepository.findAllById(branchIds).stream()
            .collect(Collectors.toMap(Branch::getId, b -> b));

        Set<UUID> teamIds = new HashSet<>();
        rooms.getContent().forEach(r -> {
            teamIds.add(r.getCreatorTeamId());
            if (r.getOpponentTeamId() != null) {
                teamIds.add(r.getOpponentTeamId());
            }
        });
        Map<UUID, Team> teams = teamRepository.findAllById(teamIds).stream()
            .collect(Collectors.toMap(Team::getId, t -> t));

        Map<UUID, Long> memberCounts = teamMemberRepository.findByTeamIdIn(new ArrayList<>(teamIds)).stream()
            .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        return rooms.map(room -> {
            Branch branch = branches.get(room.getBranchId());
            Team creatorTeam = teams.get(room.getCreatorTeamId());
            Team opponentTeam = room.getOpponentTeamId() != null ? teams.get(room.getOpponentTeamId()) : null;
            int creatorCount = memberCounts.getOrDefault(room.getCreatorTeamId(), 0L).intValue();
            int opponentCount = opponentTeam != null ? memberCounts.getOrDefault(room.getOpponentTeamId(), 0L).intValue() : 0;
            return mapToResponse(room, branch, creatorTeam, opponentTeam, creatorCount, opponentCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public TeamRoomDetailResponse getRoomById(UUID id) {
        TeamRoom room = teamRoomRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + id));

        Branch branch = branchRepository.findById(room.getBranchId())
            .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        TeamDetailResponse creatorTeam = teamService.getTeamById(room.getCreatorTeamId());
        TeamDetailResponse opponentTeam = room.getOpponentTeamId() != null
            ? teamService.getTeamById(room.getOpponentTeamId())
            : null;

        return new TeamRoomDetailResponse(
            room.getId(),
            mapBranchToResponse(branch),
            creatorTeam,
            opponentTeam,
            room.getScheduledDate().format(DATE_FORMATTER),
            room.getStartTime().format(TIME_FORMATTER),
            room.getEndTime().format(TIME_FORMATTER),
            room.getRequiredTeamSize(),
            room.getStatus().name(),
            room.getCreatedAt(),
            room.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void joinRoom(UUID roomId, JoinTeamRoomRequest request) {
        UUID currentUserId = getCurrentUserId();

        TeamRoom room = teamRoomRepository.findByIdAndStatusNot(roomId, TeamRoom.TeamRoomStatus.CANCELLED)
            .orElseThrow(() -> new NoSuchElementException("Room not found or cancelled"));

        if (room.getStatus() == TeamRoom.TeamRoomStatus.MATCHED) {
            throw new IllegalStateException("Room is already matched");
        }

        Team opponentTeam = teamRepository.findByIdAndStatus(request.teamId(), Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException("Team not found or disbanded"));

        if (!opponentTeam.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException("Only the team captain can join team rooms");
        }

        if (room.getCreatorTeamId().equals(request.teamId())) {
            throw new IllegalArgumentException("Cannot join your own team's room");
        }

        if (!opponentTeam.getRosterSize().equals(room.getRequiredTeamSize())) {
            throw new IllegalArgumentException(
                "Team size mismatch. This room requires teams of " + room.getRequiredTeamSize() + " players");
        }

        long opponentMemberCount = teamMemberRepository.countByTeamId(request.teamId());
        if (opponentMemberCount < opponentTeam.getRosterSize()) {
            throw new IllegalStateException("Team must have full roster (" + opponentTeam.getRosterSize() + " members) to join");
        }

        if (teamRoomRepository.hasTeamConflict(request.teamId(), room.getScheduledDate(), room.getStartTime(), room.getEndTime())) {
            throw new IllegalArgumentException("Your team has a conflicting team room booking at this time");
        }

        List<UUID> opponentMemberIds = teamMemberRepository.findUserIdsByTeamId(request.teamId());
        if (teamRoomRepository.hasTeamMembersIndividualConflict(opponentMemberIds, room.getScheduledDate(), room.getStartTime(), room.getEndTime())) {
            throw new IllegalArgumentException("One or more of your team members have conflicting individual room bookings at this time");
        }

        room.setOpponentTeamId(request.teamId());
        room.setStatus(TeamRoom.TeamRoomStatus.MATCHED);
        teamRoomRepository.save(room);

        log.info("Team {} joined room {} (opponent)", request.teamId(), roomId);
    }

    @Override
    @Transactional
    public void cancelRoom(UUID roomId) {
        UUID currentUserId = getCurrentUserId();

        TeamRoom room = teamRoomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("Room not found"));

        Team creatorTeam = teamRepository.findById(room.getCreatorTeamId())
            .orElseThrow(() -> new NoSuchElementException("Creator team not found"));

        if (!creatorTeam.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException("Only the creator team's captain can cancel the room");
        }

        room.setStatus(TeamRoom.TeamRoomStatus.CANCELLED);
        teamRoomRepository.save(room);
        log.info("Team room {} cancelled by creator captain {}", roomId, currentUserId);
    }

    private TeamRoomResponse mapToResponse(TeamRoom room, Branch branch, Team creatorTeam, Team opponentTeam,
                                           int creatorMemberCount, int opponentMemberCount) {
        return new TeamRoomResponse(
            room.getId(),
            mapBranchToResponse(branch),
            new TeamRoomResponse.TeamSummary(
                creatorTeam.getId(),
                creatorTeam.getName(),
                creatorTeam.getLogoUrl(),
                creatorTeam.getRosterSize(),
                creatorMemberCount
            ),
            opponentTeam != null ? new TeamRoomResponse.TeamSummary(
                opponentTeam.getId(),
                opponentTeam.getName(),
                opponentTeam.getLogoUrl(),
                opponentTeam.getRosterSize(),
                opponentMemberCount
            ) : null,
            room.getScheduledDate().format(DATE_FORMATTER),
            room.getStartTime().format(TIME_FORMATTER),
            room.getEndTime().format(TIME_FORMATTER),
            room.getRequiredTeamSize(),
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

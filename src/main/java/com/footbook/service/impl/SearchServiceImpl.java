package com.footbook.service.impl;

import com.footbook.domain.*;
import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.dto.response.room.IndividualRoomResponse;
import com.footbook.dto.response.room.TeamRoomResponse;
import com.footbook.dto.response.search.SearchResponse;
import com.footbook.dto.response.team.TeamResponse;
import com.footbook.repository.*;
import com.footbook.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final BranchRepository branchRepository;
    private final IndividualRoomRepository individualRoomRepository;
    private final TeamRoomRepository teamRoomRepository;
    private final TeamRepository teamRepository;
    private final IndividualRoomParticipantRepository participantRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(String query, String type) {
        if (query == null || query.trim().isEmpty()) {
            return new SearchResponse(List.of(), List.of(), List.of(), List.of(), 0);
        }

        String searchQuery = query.trim().toLowerCase();

        List<BranchResponse> branches = List.of();
        List<IndividualRoomResponse> individualRooms = List.of();
        List<TeamRoomResponse> teamRooms = List.of();
        List<TeamResponse> teams = List.of();

        if (type == null || type.isEmpty() || type.equalsIgnoreCase("branches")) {
            branches = searchBranches(searchQuery);
        }

        if (type == null || type.isEmpty() || type.equalsIgnoreCase("rooms") || type.equalsIgnoreCase("individual_rooms")) {
            individualRooms = searchIndividualRooms(searchQuery);
        }

        if (type == null || type.isEmpty() || type.equalsIgnoreCase("rooms") || type.equalsIgnoreCase("team_rooms")) {
            teamRooms = searchTeamRooms(searchQuery);
        }

        if (type == null || type.isEmpty() || type.equalsIgnoreCase("teams")) {
            teams = searchTeams(searchQuery);
        }

        int totalResults = branches.size() + individualRooms.size() + teamRooms.size() + teams.size();

        return new SearchResponse(branches, individualRooms, teamRooms, teams, totalResults);
    }

    private List<BranchResponse> searchBranches(String query) {
        List<Branch> branches = branchRepository.findByIsActiveTrueOrderByNameAsc().stream()
            .filter(b -> b.getName().toLowerCase().contains(query) ||
                (b.getAddress() != null && b.getAddress().toLowerCase().contains(query)))
            .limit(10)
            .toList();

        return branches.stream()
            .map(this::mapBranchToResponse)
            .toList();
    }

    private List<IndividualRoomResponse> searchIndividualRooms(String query) {
        List<IndividualRoom> rooms = individualRoomRepository.findAll().stream()
            .filter(r -> r.getStatus() != IndividualRoom.RoomStatus.CANCELLED)
            .limit(50)
            .toList();

        Set<UUID> branchIds = rooms.stream().map(IndividualRoom::getBranchId).collect(Collectors.toSet());
        Map<UUID, Branch> branches = branchRepository.findAllById(branchIds).stream()
            .collect(Collectors.toMap(Branch::getId, b -> b));

        Set<UUID> ownerIds = rooms.stream().map(IndividualRoom::getOwnerId).collect(Collectors.toSet());
        Map<UUID, User> owners = userRepository.findAllById(ownerIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        List<UUID> roomIds = rooms.stream().map(IndividualRoom::getId).toList();
        Map<UUID, Long> participantCounts = roomIds.isEmpty() ? new HashMap<>() :
            participantRepository.findByRoomIdIn(roomIds).stream()
                .collect(Collectors.groupingBy(IndividualRoomParticipant::getRoomId, Collectors.counting()));

        return rooms.stream()
            .filter(r -> {
                Branch branch = branches.get(r.getBranchId());
                User owner = owners.get(r.getOwnerId());
                String ownerName = owner.getFirstName() + " " + owner.getLastName();

                return branch.getName().toLowerCase().contains(query) ||
                    ownerName.toLowerCase().contains(query) ||
                    (r.getNotes() != null && r.getNotes().toLowerCase().contains(query));
            })
            .limit(10)
            .map(room -> {
                Branch branch = branches.get(room.getBranchId());
                User owner = owners.get(room.getOwnerId());
                Long filledSlots = participantCounts.getOrDefault(room.getId(), 0L);

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
                    filledSlots.intValue(),
                    room.getTotalSlots() - filledSlots.intValue(),
                    room.getNotes(),
                    room.getStatus().name(),
                    room.getCreatedAt(),
                    room.getUpdatedAt()
                );
            })
            .toList();
    }

    private List<TeamRoomResponse> searchTeamRooms(String query) {
        List<TeamRoom> rooms = teamRoomRepository.findAll().stream()
            .filter(r -> r.getStatus() != TeamRoom.TeamRoomStatus.CANCELLED)
            .limit(50)
            .toList();

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

        Map<UUID, Long> memberCounts = teamIds.isEmpty() ? new HashMap<>() :
            teamMemberRepository.findByTeamIdIn(new ArrayList<>(teamIds)).stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        return rooms.stream()
            .filter(r -> {
                Branch branch = branches.get(r.getBranchId());
                Team creatorTeam = teams.get(r.getCreatorTeamId());
                Team opponentTeam = r.getOpponentTeamId() != null ? teams.get(r.getOpponentTeamId()) : null;

                return branch.getName().toLowerCase().contains(query) ||
                    creatorTeam.getName().toLowerCase().contains(query) ||
                    (opponentTeam != null && opponentTeam.getName().toLowerCase().contains(query));
            })
            .limit(10)
            .map(room -> {
                Branch branch = branches.get(room.getBranchId());
                Team creatorTeam = teams.get(room.getCreatorTeamId());
                Team opponentTeam = room.getOpponentTeamId() != null ? teams.get(room.getOpponentTeamId()) : null;
                int creatorCount = memberCounts.getOrDefault(room.getCreatorTeamId(), 0L).intValue();
                int opponentCount = opponentTeam != null ? memberCounts.getOrDefault(room.getOpponentTeamId(), 0L).intValue() : 0;

                return new TeamRoomResponse(
                    room.getId(),
                    mapBranchToResponse(branch),
                    new TeamRoomResponse.TeamSummary(
                        creatorTeam.getId(),
                        creatorTeam.getName(),
                        creatorTeam.getLogoUrl(),
                        creatorTeam.getRosterSize(),
                        creatorCount
                    ),
                    opponentTeam != null ? new TeamRoomResponse.TeamSummary(
                        opponentTeam.getId(),
                        opponentTeam.getName(),
                        opponentTeam.getLogoUrl(),
                        opponentTeam.getRosterSize(),
                        opponentCount
                    ) : null,
                    room.getScheduledDate().format(DATE_FORMATTER),
                    room.getStartTime().format(TIME_FORMATTER),
                    room.getEndTime().format(TIME_FORMATTER),
                    room.getRequiredTeamSize(),
                    room.getStatus().name(),
                    room.getCreatedAt(),
                    room.getUpdatedAt()
                );
            })
            .toList();
    }

    private List<TeamResponse> searchTeams(String query) {
        List<Team> teams = teamRepository.findAll().stream()
            .filter(t -> t.getStatus() == Team.TeamStatus.ACTIVE)
            .filter(t -> t.getName().toLowerCase().contains(query) ||
                (t.getDescription() != null && t.getDescription().toLowerCase().contains(query)))
            .limit(10)
            .toList();

        List<UUID> teamIds = teams.stream().map(Team::getId).toList();
        Map<UUID, Long> memberCounts = teamIds.isEmpty() ? new HashMap<>() :
            teamMemberRepository.findByTeamIdIn(teamIds).stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        Set<UUID> captainIds = teams.stream().map(Team::getCaptainId).collect(Collectors.toSet());
        Map<UUID, User> captains = userRepository.findAllById(captainIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        return teams.stream()
            .map(team -> {
                User captain = captains.get(team.getCaptainId());
                Long memberCount = memberCounts.getOrDefault(team.getId(), 0L);

                return new TeamResponse(
                    team.getId(),
                    team.getName(),
                    team.getDescription(),
                    team.getLogoUrl(),
                    new TeamResponse.CaptainSummary(
                        captain.getId(),
                        captain.getFirstName(),
                        captain.getLastName(),
                        captain.getProfilePictureUrl()
                    ),
                    team.getRosterSize(),
                    memberCount.intValue(),
                    team.getRosterSize() - memberCount.intValue(),
                    team.getStatus().name(),
                    team.getCreatedAt(),
                    team.getUpdatedAt()
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
}

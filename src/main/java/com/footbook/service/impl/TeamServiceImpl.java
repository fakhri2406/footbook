package com.footbook.service.impl;

import com.footbook.domain.Team;
import com.footbook.domain.TeamMember;
import com.footbook.domain.User;
import com.footbook.dto.request.team.AddMemberRequest;
import com.footbook.dto.request.team.CreateTeamRequest;
import com.footbook.dto.request.team.TransferCaptainRequest;
import com.footbook.dto.request.team.UpdateTeamRequest;
import com.footbook.dto.response.team.TeamDetailResponse;
import com.footbook.dto.response.team.TeamResponse;
import com.footbook.repository.TeamMemberRepository;
import com.footbook.repository.TeamRepository;
import com.footbook.repository.UserRepository;
import com.footbook.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.footbook.util.ErrorMessages.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request) {
        UUID currentUserId = getCurrentUserId();

        Team team = Team.builder()
            .name(request.name())
            .description(request.description())
            .logoUrl(request.logoUrl())
            .captainId(currentUserId)
            .rosterSize(request.rosterSize())
            .status(Team.TeamStatus.ACTIVE)
            .build();

        team = teamRepository.save(team);

        TeamMember captainMember = TeamMember.builder()
            .teamId(team.getId())
            .userId(currentUserId)
            .joinedAt(LocalDateTime.now())
            .build();

        teamMemberRepository.save(captainMember);

        log.info("Created team {} with captain {}", team.getId(), currentUserId);

        return mapToResponse(team, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamResponse> getAllTeams(String name, Pageable pageable) {
        Page<Team> teams = teamRepository.findActiveTeams(name, pageable);

        List<UUID> teamIds = teams.getContent().stream().map(Team::getId).toList();
        Map<UUID, Long> memberCounts = teamIds.isEmpty() ? new HashMap<>() :
            teamMemberRepository.findByTeamIdIn(teamIds).stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        return teams.map(team -> {
            Long memberCount = memberCounts.getOrDefault(team.getId(), 0L);
            return mapToResponse(team, memberCount.intValue());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDetailResponse getTeamById(UUID id) {
        Team team = teamRepository.findByIdAndStatus(id, Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException(TEAM_NOT_FOUND + " with ID: " + id));

        List<TeamMember> members = teamMemberRepository.findByTeamIdOrderByJoinedAtAsc(id);

        Set<UUID> userIds = members.stream().map(TeamMember::getUserId).collect(Collectors.toSet());
        userIds.add(team.getCaptainId());
        Map<UUID, User> users = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));

        User captain = users.get(team.getCaptainId());
        List<TeamDetailResponse.MemberInfo> memberInfos = members.stream()
            .map(m -> {
                User user = users.get(m.getUserId());
                return new TeamDetailResponse.MemberInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getProfilePictureUrl(),
                    m.getJoinedAt()
                );
            })
            .toList();

        return new TeamDetailResponse(
            team.getId(),
            team.getName(),
            team.getDescription(),
            team.getLogoUrl(),
            new TeamDetailResponse.CaptainInfo(
                captain.getId(),
                captain.getFirstName(),
                captain.getLastName(),
                captain.getEmail(),
                captain.getProfilePictureUrl()
            ),
            team.getRosterSize(),
            members.size(),
            team.getRosterSize() - members.size(),
            team.getStatus().name(),
            memberInfos,
            team.getCreatedAt(),
            team.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeamsAsCaptain() {
        UUID currentUserId = getCurrentUserId();
        List<Team> teams = teamRepository.findByCaptainIdAndStatusOrderByCreatedAtDesc(
            currentUserId, Team.TeamStatus.ACTIVE);

        List<UUID> teamIds = teams.stream().map(Team::getId).toList();
        Map<UUID, Long> memberCounts = teamIds.isEmpty() ? new HashMap<>() :
            teamMemberRepository.findByTeamIdIn(teamIds).stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        return teams.stream()
            .map(team -> {
                Long memberCount = memberCounts.getOrDefault(team.getId(), 0L);
                return mapToResponse(team, memberCount.intValue());
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getMyTeamsAsMember() {
        UUID currentUserId = getCurrentUserId();
        List<Team> teams = teamRepository.findTeamsByMember(currentUserId);

        List<UUID> teamIds = teams.stream().map(Team::getId).toList();
        Map<UUID, Long> memberCounts = teamIds.isEmpty() ? new HashMap<>() :
            teamMemberRepository.findByTeamIdIn(teamIds).stream()
                .collect(Collectors.groupingBy(TeamMember::getTeamId, Collectors.counting()));

        return teams.stream()
            .map(team -> {
                Long memberCount = memberCounts.getOrDefault(team.getId(), 0L);
                return mapToResponse(team, memberCount.intValue());
            })
            .toList();
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(UUID id, UpdateTeamRequest request) {
        UUID currentUserId = getCurrentUserId();

        Team team = teamRepository.findByIdAndStatus(id, Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException(TEAM_NOT_FOUND + " with ID: " + id));

        if (!team.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException(NOT_CAPTAIN);
        }

        if (request.name() != null) {
            team.setName(request.name());
        }
        if (request.description() != null) {
            team.setDescription(request.description());
        }
        if (request.logoUrl() != null) {
            team.setLogoUrl(request.logoUrl());
        }

        team = teamRepository.save(team);
        log.info("Updated team {} by captain {}", team.getId(), currentUserId);

        long memberCount = teamMemberRepository.countByTeamId(id);
        return mapToResponse(team, (int) memberCount);
    }

    @Override
    @Transactional
    public void addMember(UUID id, AddMemberRequest request) {
        UUID currentUserId = getCurrentUserId();

        Team team = teamRepository.findByIdAndStatus(id, Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException(TEAM_NOT_FOUND + " with ID: " + id));

        if (!team.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException(NOT_CAPTAIN);
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(id, request.userId())) {
            throw new IllegalArgumentException(ALREADY_MEMBER);
        }

        long currentMemberCount = teamMemberRepository.countByTeamId(id);
        if (currentMemberCount >= team.getRosterSize()) {
            throw new IllegalStateException(TEAM_FULL);
        }

        TeamMember teamMember = TeamMember.builder()
            .teamId(id)
            .userId(request.userId())
            .joinedAt(LocalDateTime.now())
            .build();

        teamMemberRepository.save(teamMember);
        log.info("Added user {} to team {} by captain {}", request.userId(), id, currentUserId);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        UUID currentUserId = getCurrentUserId();

        Team team = teamRepository.findByIdAndStatus(teamId, Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException(TEAM_NOT_FOUND + " with ID: " + teamId));

        if (!team.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException(NOT_CAPTAIN);
        }

        if (team.getCaptainId().equals(userId)) {
            throw new IllegalStateException(CANNOT_REMOVE_CAPTAIN);
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new NoSuchElementException(NOT_TEAM_MEMBER);
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
        log.info("Removed user {} from team {} by captain {}", userId, teamId, currentUserId);
    }

    @Override
    @Transactional
    public void transferCaptain(UUID id, TransferCaptainRequest request) {
        UUID currentUserId = getCurrentUserId();

        Team team = teamRepository.findByIdAndStatus(id, Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException(TEAM_NOT_FOUND + " with ID: " + id));

        if (!team.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException(NOT_CAPTAIN);
        }

        if (request.newCaptainId().equals(currentUserId)) {
            throw new IllegalArgumentException(ALREADY_CAPTAIN);
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(id, request.newCaptainId())) {
            throw new IllegalArgumentException(NEW_CAPTAIN_NOT_MEMBER);
        }

        userRepository.findById(request.newCaptainId())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND + " with ID: " + request.newCaptainId()));

        team.setCaptainId(request.newCaptainId());
        teamRepository.save(team);
        log.info("Transferred captain role in team {} from {} to {}", id, currentUserId, request.newCaptainId());
    }

    @Override
    @Transactional
    public void disbandTeam(UUID id) {
        UUID currentUserId = getCurrentUserId();

        Team team = teamRepository.findByIdAndStatus(id, Team.TeamStatus.ACTIVE)
            .orElseThrow(() -> new NoSuchElementException(TEAM_NOT_FOUND + " with ID: " + id));

        if (!team.getCaptainId().equals(currentUserId)) {
            throw new IllegalStateException(NOT_CAPTAIN);
        }

        team.setStatus(Team.TeamStatus.DISBANDED);
        teamRepository.save(team);
        log.info("Team {} disbanded by captain {}", id, currentUserId);
    }

    private TeamResponse mapToResponse(Team team, int memberCount) {
        User captain = userRepository.findById(team.getCaptainId())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

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
            memberCount,
            team.getRosterSize() - memberCount,
            team.getStatus().name(),
            team.getCreatedAt(),
            team.getUpdatedAt()
        );
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND))
            .getId();
    }
}

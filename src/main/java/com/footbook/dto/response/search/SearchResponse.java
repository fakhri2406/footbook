package com.footbook.dto.response.search;

import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.dto.response.room.IndividualRoomResponse;
import com.footbook.dto.response.room.TeamRoomResponse;
import com.footbook.dto.response.team.TeamResponse;

import java.util.List;

public record SearchResponse(
    List<BranchResponse> branches,
    List<IndividualRoomResponse> individualRooms,
    List<TeamRoomResponse> teamRooms,
    List<TeamResponse> teams,
    int totalResults
) {
}

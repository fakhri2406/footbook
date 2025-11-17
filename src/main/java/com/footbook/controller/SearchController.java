package com.footbook.controller;

import com.footbook.dto.response.search.SearchResponse;
import com.footbook.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@Validated
@RequiredArgsConstructor
@Tag(name = "Search", description = "Global search endpoints")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    @Operation(
        summary = "Global search",
        description = "Search across branches, rooms, and teams. Optional type filter: 'branches', 'rooms', 'teams', 'individual_rooms', or 'team_rooms'"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    public ResponseEntity<SearchResponse> search(
        @RequestParam String query,
        @RequestParam(required = false) String type) {
        return ResponseEntity.ok(searchService.search(query, type));
    }
}

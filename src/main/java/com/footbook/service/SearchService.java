package com.footbook.service;

import com.footbook.dto.response.search.SearchResponse;

/**
 * Service interface for global search operations.
 */
public interface SearchService {
    /**
     * Search across all entities (branches, rooms, teams)
     *
     * @param query search query string
     * @param type  optional filter by type (branches, rooms, teams, individual_rooms, team_rooms)
     * @return search results
     */
    SearchResponse search(String query, String type);
}

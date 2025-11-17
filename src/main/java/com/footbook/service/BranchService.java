package com.footbook.service;

import com.footbook.dto.request.branch.CreateBranchRequest;
import com.footbook.dto.request.branch.UpdateBranchRequest;
import com.footbook.dto.response.branch.BranchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for branch management operations.
 * Handles CRUD operations for stadium branches.
 */
public interface BranchService {
    /**
     * Get all active branches with pagination and optional filtering
     *
     * @param name     optional name filter
     * @param pageable pagination information
     * @return paginated list of branches
     */
    Page<BranchResponse> getAllBranches(String name, Pageable pageable);

    /**
     * Get all active branches without pagination
     *
     * @return list of all active branches
     */
    List<BranchResponse> getAllActiveBranches();

    /**
     * Get branch by ID
     *
     * @param id branch ID
     * @return branch details
     * @throws java.util.NoSuchElementException if branch not found
     */
    BranchResponse getBranchById(UUID id);

    /**
     * Create a new branch (ADMIN only)
     *
     * @param request branch creation request
     * @return created branch details
     */
    BranchResponse createBranch(CreateBranchRequest request);

    /**
     * Update an existing branch (ADMIN only)
     *
     * @param id      branch ID
     * @param request branch update request
     * @return updated branch details
     * @throws java.util.NoSuchElementException if branch not found
     */
    BranchResponse updateBranch(UUID id, UpdateBranchRequest request);

    /**
     * Delete (soft delete) a branch (ADMIN only)
     *
     * @param id branch ID
     * @throws java.util.NoSuchElementException if branch not found
     */
    void deleteBranch(UUID id);
}

package com.footbook.repository;

import com.footbook.domain.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    @Query("SELECT b FROM Branch b WHERE b.isActive = true " +
        "AND (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Branch> findActiveBranches(@Param("name") String name, Pageable pageable);

    Optional<Branch> findByIdAndIsActiveTrue(UUID id);

    List<Branch> findByIsActiveTrueOrderByNameAsc();

    boolean existsByIdAndIsActiveTrue(UUID id);
}

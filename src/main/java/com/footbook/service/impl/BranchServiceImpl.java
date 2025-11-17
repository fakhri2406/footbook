package com.footbook.service.impl;

import com.footbook.domain.Branch;
import com.footbook.dto.request.branch.CreateBranchRequest;
import com.footbook.dto.request.branch.UpdateBranchRequest;
import com.footbook.dto.response.branch.BranchResponse;
import com.footbook.repository.BranchRepository;
import com.footbook.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.footbook.util.ErrorMessages.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponse> getAllBranches(String name, Pageable pageable) {
        return branchRepository.findActiveBranches(name, pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllActiveBranches() {
        return branchRepository.findByIsActiveTrueOrderByNameAsc()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new NoSuchElementException(BRANCH_NOT_FOUND + " with ID: " + id));
        return mapToResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        LocalTime startTime = parseTime(request.operatingHoursStart(), "Operating hours start");
        LocalTime endTime = parseTime(request.operatingHoursEnd(), "Operating hours end");

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(OPERATING_HOURS_INVALID);
        }

        Branch branch = Branch.builder()
            .name(request.name())
            .address(request.address())
            .googleMapsUrl(request.googleMapsUrl())
            .operatingHoursStart(startTime)
            .operatingHoursEnd(endTime)
            .contactPhone(request.contactPhone())
            .contactEmail(request.contactEmail())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .isActive(true)
            .build();

        branch = branchRepository.save(branch);
        log.info("Created new branch: {} with ID: {}", branch.getName(), branch.getId());

        return mapToResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(BRANCH_NOT_FOUND + " with ID: " + id));

        if (request.name() != null) {
            branch.setName(request.name());
        }
        if (request.address() != null) {
            branch.setAddress(request.address());
        }
        if (request.googleMapsUrl() != null) {
            branch.setGoogleMapsUrl(request.googleMapsUrl());
        }
        if (request.operatingHoursStart() != null) {
            LocalTime startTime = parseTime(request.operatingHoursStart(), "Operating hours start");
            branch.setOperatingHoursStart(startTime);
        }
        if (request.operatingHoursEnd() != null) {
            LocalTime endTime = parseTime(request.operatingHoursEnd(), "Operating hours end");
            branch.setOperatingHoursEnd(endTime);
        }
        if (request.contactPhone() != null) {
            branch.setContactPhone(request.contactPhone());
        }
        if (request.contactEmail() != null) {
            branch.setContactEmail(request.contactEmail());
        }
        if (request.latitude() != null) {
            branch.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            branch.setLongitude(request.longitude());
        }
        if (request.isActive() != null) {
            branch.setIsActive(request.isActive());
        }

        if (!branch.getOperatingHoursEnd().isAfter(branch.getOperatingHoursStart())) {
            throw new IllegalArgumentException(OPERATING_HOURS_INVALID);
        }

        branch = branchRepository.save(branch);
        log.info("Updated branch: {} with ID: {}", branch.getName(), branch.getId());

        return mapToResponse(branch);
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        Branch branch = branchRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(BRANCH_NOT_FOUND + " with ID: " + id));

        branch.setIsActive(false);
        branchRepository.save(branch);
        log.info("Soft deleted branch with ID: {}", id);
    }

    private BranchResponse mapToResponse(Branch branch) {
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

    private LocalTime parseTime(String timeStr, String fieldName) {
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " " + TIME_FORMAT_INVALID);
        }
    }
}

package com.pharmacy.management.repository;

import com.pharmacy.management.entity.PharmacyBranch;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PharmacyBranchRepository extends JpaRepository<PharmacyBranch, Integer> {

    List<PharmacyBranch> findByBranchNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String branchName,
            String address,
            Sort sort
    );

    List<PharmacyBranch> findByActiveTrueOrderByBranchNameAsc();

    Optional<PharmacyBranch> findByBranchNameIgnoreCase(String branchName);
}


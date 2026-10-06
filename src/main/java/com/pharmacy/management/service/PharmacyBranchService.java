package com.pharmacy.management.service;

import com.pharmacy.management.entity.PharmacyBranch;
import com.pharmacy.management.repository.PharmacyBranchRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PharmacyBranchService {

    private final PharmacyBranchRepository pharmacyBranchRepository;

    public PharmacyBranchService(PharmacyBranchRepository pharmacyBranchRepository) {
        this.pharmacyBranchRepository = pharmacyBranchRepository;
    }

    public List<PharmacyBranch> findAll(String keyword) {
        Sort sort = Sort.by(Sort.Direction.ASC, "branchName");
        if (keyword == null || keyword.isBlank()) {
            return pharmacyBranchRepository.findAll(sort);
        }
        String text = keyword.trim();
        return pharmacyBranchRepository.findByBranchNameContainingIgnoreCaseOrAddressContainingIgnoreCase(text, text, sort);
    }

    public PharmacyBranch findById(Integer branchId) {
        return pharmacyBranchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found."));
    }

    @Transactional
    public void create(PharmacyBranch branch) {
        branch.setBranchId(null);
        pharmacyBranchRepository.save(branch);
    }

    @Transactional
    public void update(Integer branchId, PharmacyBranch formBranch) {
        PharmacyBranch existing = findById(branchId);
        existing.setBranchName(formBranch.getBranchName());
        existing.setAddress(formBranch.getAddress());
        existing.setContactNumber(formBranch.getContactNumber());
        existing.setManagerName(formBranch.getManagerName());
        existing.setActive(formBranch.isActive());
        pharmacyBranchRepository.save(existing);
    }

    @Transactional
    public void delete(Integer branchId) {
        PharmacyBranch branch = findById(branchId);
        branch.setActive(false);
        pharmacyBranchRepository.save(branch);
    }
}

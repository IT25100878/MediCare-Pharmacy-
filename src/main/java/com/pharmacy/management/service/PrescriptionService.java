package com.pharmacy.management.service;

import com.pharmacy.management.entity.Prescription;
import com.pharmacy.management.repository.PrescriptionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final CurrentUserService currentUserService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               CurrentUserService currentUserService) {
        this.prescriptionRepository = prescriptionRepository;
        this.currentUserService = currentUserService;
    }

    public List<Prescription> findAll(String keyword, String status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Prescription> records;
        if (keyword == null || keyword.isBlank()) {
            records = prescriptionRepository.findAll(sort);
        } else {
            String text = keyword.trim();
            records = prescriptionRepository
                    .findByPrescriptionNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrCustomerPhoneContainingIgnoreCase(
                            text, text, text, sort
                    );
        }
        String safeStatus = status == null ? "" : status.trim().toUpperCase();
        return records.stream()
                .filter(record -> !record.isArchived())
                .filter(record -> safeStatus.isBlank() || safeStatus.equals(record.getPrescriptionStatus()))
                .toList();
    }

    public Prescription findById(Integer prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found."));
    }

    @Transactional
    public void create(Prescription prescription, String currentUserEmail) {
        prescription.setPrescriptionId(null);
        prescription.setPharmacistUserId(currentUserService.getUserId(currentUserEmail));
        setReviewTime(prescription);
        prescriptionRepository.save(prescription);
    }

    @Transactional
    public void review(Integer prescriptionId, String status, String pharmacistNote, String currentUserEmail) {
        Prescription existing = findById(prescriptionId);
        if (!"PENDING".equals(status) && !"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new IllegalArgumentException("Choose a valid prescription decision.");
        }
        if ("REJECTED".equals(status) && (pharmacistNote == null || pharmacistNote.isBlank())) {
            throw new IllegalArgumentException("Enter a clear reason before rejecting a prescription.");
        }
        existing.setPrescriptionStatus(status);
        existing.setPharmacistNote(pharmacistNote == null || pharmacistNote.isBlank() ? null : pharmacistNote.trim());
        existing.setPharmacistUserId(currentUserService.getUserId(currentUserEmail));
        setReviewTime(existing);
        prescriptionRepository.save(existing);
    }

    @Transactional
    public void delete(Integer prescriptionId) {
        Prescription prescription = findById(prescriptionId);
        prescription.setArchived(true);
        prescriptionRepository.save(prescription);
    }

    private void setReviewTime(Prescription prescription) {
        if ("APPROVED".equals(prescription.getPrescriptionStatus())
                || "REJECTED".equals(prescription.getPrescriptionStatus())) {
            prescription.setReviewedAt(LocalDateTime.now());
        } else {
            prescription.setReviewedAt(null);
        }
    }
}

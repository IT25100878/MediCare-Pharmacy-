package com.pharmacy.management.service;

import com.pharmacy.management.entity.Medicine;
import com.pharmacy.management.repository.MedicineRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicineComplianceService {

    private final MedicineRepository medicineRepository;

    public MedicineComplianceService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public List<Medicine> findAll() {
        return medicineRepository.findAll(Sort.by(Sort.Direction.ASC, "medicineName", "batchNumber"));
    }

    @Transactional
    public void updatePrescriptionRequirement(Integer medicineId, boolean requiresPrescription) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found."));
        medicine.setRequiresPrescription(requiresPrescription);
        medicine.setUpdatedAt(LocalDateTime.now());
        medicineRepository.save(medicine);
    }
}

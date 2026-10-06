package com.pharmacy.management.repository;

import com.pharmacy.management.entity.Prescription;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    List<Prescription> findByPrescriptionNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrCustomerPhoneContainingIgnoreCase(
            String prescriptionNumber,
            String customerName,
            String customerPhone,
            Sort sort
    );
}

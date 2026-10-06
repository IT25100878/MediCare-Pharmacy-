package com.pharmacy.management.repository;

import com.pharmacy.management.entity.Medicine;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Integer> {

    List<Medicine> findByMedicineNameContainingIgnoreCaseOrGenericNameContainingIgnoreCaseOrBatchNumberContainingIgnoreCase(
            String medicineName,
            String genericName,
            String batchNumber,
            Sort sort
    );

    @Query("select count(m) from Medicine m where m.quantityInStock <= m.reorderLevel")
    long countLowStockMedicines();

    @Query("select count(m) from Medicine m where m.expiryDate <= :alertDate")
    long countMedicinesExpiringBy(LocalDate alertDate);
}


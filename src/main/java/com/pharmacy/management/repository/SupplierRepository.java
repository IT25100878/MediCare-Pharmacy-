package com.pharmacy.management.repository;

import com.pharmacy.management.entity.Supplier;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    List<Supplier> findBySupplierNameContainingIgnoreCaseOrContactPersonContainingIgnoreCaseOrContactNumberContainingIgnoreCase(
            String supplierName,
            String contactPerson,
            String contactNumber,
            Sort sort
    );
}
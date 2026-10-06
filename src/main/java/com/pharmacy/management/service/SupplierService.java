package com.pharmacy.management.service;

import com.pharmacy.management.entity.Supplier;
import com.pharmacy.management.repository.SupplierRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll(String keyword) {
        Sort sort = Sort.by(Sort.Direction.ASC, "supplierName");
        if (keyword == null || keyword.isBlank()) {
            return supplierRepository.findAll(sort);
        }
        String text = keyword.trim();
        return supplierRepository
                .findBySupplierNameContainingIgnoreCaseOrContactPersonContainingIgnoreCaseOrContactNumberContainingIgnoreCase(
                        text, text, text, sort
                );
    }

    public Supplier findById(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found."));
    }

    @Transactional
    public void create(Supplier supplier) {
        supplier.setSupplierId(null);
        supplier.setBusinessRegistrationId(trimToNull(supplier.getBusinessRegistrationId()));
        supplier.setMedicineCategory(trimToNull(supplier.getMedicineCategory()));
        supplier.setPaymentTerms(trimToNull(supplier.getPaymentTerms()));
        supplierRepository.save(supplier);
    }

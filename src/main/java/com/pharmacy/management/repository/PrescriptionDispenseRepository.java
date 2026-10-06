package com.pharmacy.management.repository;

import com.pharmacy.management.dto.ApprovedPrescriptionOption;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PrescriptionDispenseRepository {

    private final JdbcTemplate jdbcTemplate;

    public PrescriptionDispenseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ApprovedPrescriptionOption> findApprovedUndispensed() {
        return jdbcTemplate.query(
                "SELECT PrescriptionId, PrescriptionNumber, CustomerName, CustomerPhone "
                        + "FROM dbo.Prescriptions "
                        + "WHERE PrescriptionStatus = N'APPROVED' AND DispensedAt IS NULL AND IsArchived = 0 "
                        + "ORDER BY ReviewedAt DESC, PrescriptionId DESC",
                (row, index) -> new ApprovedPrescriptionOption(
                        row.getInt("PrescriptionId"),
                        row.getString("PrescriptionNumber"),
                        row.getString("CustomerName"),
                        row.getString("CustomerPhone")
                )
        );
    }

    public boolean isAlreadyDispensed(Integer prescriptionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM dbo.Prescriptions WHERE PrescriptionId = ? AND DispensedAt IS NOT NULL AND IsArchived = 0",
                Integer.class,
                prescriptionId
        );
        return count != null && count > 0;
    }

    public void markDispensed(Integer prescriptionId, Integer orderId) {
        int updated = jdbcTemplate.update(
                "UPDATE dbo.Prescriptions "
                        + "SET DispensedAt = SYSDATETIME(), DispensedOrderId = ? "
                        + "WHERE PrescriptionId = ? AND PrescriptionStatus = N'APPROVED' AND DispensedAt IS NULL AND IsArchived = 0",
                orderId, prescriptionId
        );
        if (updated != 1) {
            throw new IllegalStateException("This prescription is no longer available for dispensing.");
        }
    }
}

package com.pharmacy.management.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Runs the two SQL Server procedures that intentionally remove a record and
 * its direct dependent rows. Keeping this ordering in SQL Server prevents a
 * foreign-key error when a user chooses the permanent Delete action.
 */
@Repository
public class PermanentDeletionRepository {

    private final JdbcTemplate jdbcTemplate;

    public PermanentDeletionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteMedicine(Integer medicineId) {
        jdbcTemplate.update(
                "EXEC dbo.usp_DeleteMedicinePermanently @MedicineId = ?",
                medicineId
        );
    }

    public void deletePromotion(Integer promotionId) {
        jdbcTemplate.update(
                "EXEC dbo.usp_DeletePromotionPermanently @PromotionId = ?",
                promotionId
        );
    }
}

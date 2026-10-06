package com.pharmacy.management.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class CashierCheckoutLinkRepository {

    private final JdbcTemplate jdbcTemplate;

    public CashierCheckoutLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void attachCheckout(Integer orderId,
                               Integer branchId,
                               Integer prescriptionId,
                               String paymentMethod) {
        int updated = jdbcTemplate.update(
                "UPDATE dbo.CustomerOrders "
                        + "SET BranchId = ?, PrescriptionId = ?, PaymentMethod = ?, FulfilledAt = SYSDATETIME() "
                        + "WHERE OrderId = ?",
                statement -> {
                    statement.setInt(1, branchId);
                    if (prescriptionId == null) {
                        statement.setNull(2, Types.INTEGER);
                    } else {
                        statement.setInt(2, prescriptionId);
                    }
                    statement.setString(3, paymentMethod);
                    statement.setInt(4, orderId);
                }
        );
        if (updated != 1) {
            throw new IllegalStateException("The checkout order could not be linked to its branch.");
        }
    }
}

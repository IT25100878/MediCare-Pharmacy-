package com.pharmacy.management.repository;

import com.pharmacy.management.entity.BranchStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BranchStockRepository extends JpaRepository<BranchStock, Integer> {

    Optional<BranchStock> findByBranch_BranchIdAndBatch_BatchId(Integer branchId, Integer batchId);

    @Query("select bs from BranchStock bs where bs.batch.medicine.medicineId = :medicineId order by bs.batch.expiryDate asc, bs.branch.branchName asc")
    List<BranchStock> findByMedicineId(@Param("medicineId") Integer medicineId);

    @Query("select bs from BranchStock bs order by bs.batch.medicine.medicineName asc, bs.batch.expiryDate asc, bs.branch.branchName asc")
    List<BranchStock> findAllForInventory();
}

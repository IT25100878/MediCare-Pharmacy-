package com.pharmacy.management.repository;

import com.pharmacy.management.entity.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Integer> {

    Optional<MedicineBatch> findByMedicine_MedicineIdAndBatchNumberIgnoreCase(Integer medicineId, String batchNumber);

    List<MedicineBatch> findByMedicine_MedicineIdOrderByExpiryDateAsc(Integer medicineId);

    List<MedicineBatch> findByActiveTrueOrderByExpiryDateAsc();

    long countByActiveTrue();

    @Query("select b from MedicineBatch b where b.active = true and b.availableQuantity > 0 and b.expiryDate <= :alertDate order by b.expiryDate asc")
    List<MedicineBatch> findExpiringBy(@Param("alertDate") LocalDate alertDate);
}

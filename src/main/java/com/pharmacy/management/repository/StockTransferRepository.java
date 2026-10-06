package com.pharmacy.management.repository;

import com.pharmacy.management.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Integer> {

    List<StockTransfer> findAllByOrderByRequestedAtDesc();

    long countByTransferStatus(String transferStatus);
}

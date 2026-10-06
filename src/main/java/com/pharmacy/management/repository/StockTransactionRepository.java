package com.pharmacy.management.repository;

import com.pharmacy.management.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Integer> {

    @Query("select t from StockTransaction t order by t.createdAt desc")
    List<StockTransaction> findRecentTransactions();
}

package com.personalservice.repository;

import com.personalservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByUserId(UUID userId);
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
    List<Transaction> findByUserIdAndTransactionDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.categoryId = :categoryId AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    List<Transaction> findByUserIdAndCategoryIdAndDateBetween(@Param("userId") UUID userId, @Param("categoryId") UUID categoryId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

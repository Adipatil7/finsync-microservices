package com.settlementservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settlementservice.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement,UUID> {

    boolean existsByExpenseId(UUID expenseId);
}

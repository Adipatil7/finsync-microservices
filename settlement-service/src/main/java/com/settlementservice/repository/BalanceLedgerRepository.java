package com.settlementservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settlementservice.entity.BalanceLedger;

public interface BalanceLedgerRepository extends JpaRepository<BalanceLedger, UUID> {

    List<BalanceLedger> findByGroupId(UUID groupId);

    Optional<BalanceLedger> findByGroupIdAndDebtorIdAndCreditorId(
            UUID groupId, UUID debtorId, UUID creditorId);
}

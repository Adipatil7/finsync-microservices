package com.groupservice.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.groupservice.entity.GroupExpense;

public interface GroupExpenseRepository extends JpaRepository<GroupExpense,UUID> {

    List<GroupExpense> findByGroupId(UUID groupId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM GroupExpense e WHERE e.groupId = :groupId")
    BigDecimal sumAmountByGroupId(UUID groupId);

}

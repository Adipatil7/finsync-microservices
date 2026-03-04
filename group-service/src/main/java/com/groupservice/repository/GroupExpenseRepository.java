package com.groupservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groupservice.entity.GroupExpense;

public interface GroupExpenseRepository extends JpaRepository<GroupExpense,UUID> {
    
}

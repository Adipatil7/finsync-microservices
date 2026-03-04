package com.groupservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groupservice.entity.GroupExpenseSplit;

public interface GroupExpenseSplitRepository extends JpaRepository<GroupExpenseSplit , UUID> {
    
}

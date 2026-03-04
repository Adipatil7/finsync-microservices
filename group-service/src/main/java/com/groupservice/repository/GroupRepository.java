package com.groupservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groupservice.entity.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    
}

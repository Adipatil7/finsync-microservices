package com.groupservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.groupservice.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

}

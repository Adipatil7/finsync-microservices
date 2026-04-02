package com.groupservice.repository;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.groupservice.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    @Query("SELECT gm.userId FROM GroupMember gm WHERE gm.groupId = :groupId")
    Set<UUID> findUserIdsByGroupId(UUID groupId);

    List<GroupMember> findByUserId(UUID userId);

    List<GroupMember> findByGroupId(UUID groupId);

    long countByGroupId(UUID groupId);

    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);

    long countByGroupIdAndRole(UUID groupId, String role);

}

package com.groupservice.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.groupservice.entity.GroupMember;
import com.groupservice.enums.GroupRole;
import com.groupservice.exception.AccessDeniedException;
import com.groupservice.repository.GroupMemberRepository;

@Service
public class GroupAuthorizationService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    /**
     * Throws AccessDeniedException if the user is not an ADMIN of the group.
     */
    public void requireAdmin(UUID groupId, UUID userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "User " + userId + " is not a member of group " + groupId));

        if (!GroupRole.ADMIN.name().equals(member.getRole())) {
            throw new AccessDeniedException(
                    "User " + userId + " is not an ADMIN of group " + groupId);
        }
    }

    /**
     * Throws AccessDeniedException if the user is not a member of the group.
     */
    public void requireMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException(
                    "User " + userId + " is not a member of group " + groupId);
        }
    }

    /**
     * Returns true if there is only one ADMIN left in the group.
     */
    public boolean isLastAdmin(UUID groupId) {
        return groupMemberRepository.countByGroupIdAndRole(groupId, GroupRole.ADMIN.name()) <= 1;
    }
}

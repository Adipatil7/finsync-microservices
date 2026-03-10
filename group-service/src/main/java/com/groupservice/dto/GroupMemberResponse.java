package com.groupservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.groupservice.entity.GroupMember;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {

    private UUID id;
    private UUID groupId;
    private UUID userId;
    private String role;
    private LocalDateTime joinedAt;
    private String name;

    public static GroupMemberResponse from(GroupMember member, String name) {
        return new GroupMemberResponse(
                member.getId(),
                member.getGroupId(),
                member.getUserId(),
                member.getRole(),
                member.getJoinedAt(),
                name);
    }
}

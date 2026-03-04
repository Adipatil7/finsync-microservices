package com.groupservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.groupservice.entity.Group;

public class GroupResponse {

    private UUID id;
    private String name;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public GroupResponse(UUID id, String name, UUID createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCreatedBy(),
                group.getCreatedAt()
        );
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
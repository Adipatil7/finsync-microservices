package com.groupservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.groupservice.entity.Group;

public class GroupResponse {

    private UUID id;
    private String name;
    private String description;
    private String currency;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private int memberCount;
    private BigDecimal totalSpent;

    public GroupResponse(UUID id, String name, String currency, UUID createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.memberCount = 0;
        this.totalSpent = BigDecimal.ZERO;
    }

    public GroupResponse(UUID id, String name, String currency, UUID createdBy, LocalDateTime createdAt, int memberCount,
            BigDecimal totalSpent) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
    }

    public static GroupResponse from(Group group) {
        GroupResponse response = new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCurrency(),
                group.getCreatedBy(),
                group.getCreatedAt());
        response.setDescription(group.getDescription());
        return response;
    }

    public static GroupResponse from(Group group, int memberCount, BigDecimal totalSpent) {
        GroupResponse response = new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCurrency(),
                group.getCreatedBy(),
                group.getCreatedAt(),
                memberCount,
                totalSpent);
        response.setDescription(group.getDescription());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
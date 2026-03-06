package com.personalservice.dto;

import com.personalservice.enums.TransactionType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CategoryResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private TransactionType type;
    private LocalDateTime createdAt;
}

package com.personalservice.dto;

import com.personalservice.enums.BudgetPeriod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BudgetResponse {
    private UUID id;
    private UUID userId;
    private UUID categoryId;
    private BigDecimal limitAmount;
    private BudgetPeriod period;
    private LocalDateTime createdAt;
    
    // Additional fields for context
    private String categoryName;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
}

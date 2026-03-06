package com.personalservice.dto;

import com.personalservice.enums.BudgetPeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BudgetRequest {
    private UUID categoryId; // nullable for overall budgets

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal limitAmount;

    @NotNull(message = "Period is required")
    private BudgetPeriod period;
}

package com.groupservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.groupservice.enums.SplitType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateExpenseRequest {

    @NotNull
    private UUID paidBy;

    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotNull
    private String currency;

    private String description;

    private String category;

    @NotNull
    private LocalDate expenseDate;

    @NotNull
    private SplitType splitType;

    @NotEmpty
    private List<ExpenseSplitRequest> splits;

}

package com.personalservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDayExpenseResponse {
    private LocalDate date;
    private BigDecimal totalExpenseAmount;
    private List<TransactionResponse> expenses;
}

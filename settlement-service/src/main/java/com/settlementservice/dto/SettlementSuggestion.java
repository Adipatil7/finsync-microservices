package com.settlementservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementSuggestion {

    private UUID fromUserId;
    private UUID toUserId;
    private BigDecimal amount;
    private String currency;
}

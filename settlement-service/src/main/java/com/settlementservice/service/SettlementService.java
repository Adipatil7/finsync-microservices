package com.settlementservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.settlementservice.dto.BalanceResponse;
import com.settlementservice.dto.SettlementSuggestion;

public interface SettlementService {

    void processExpenseEvent(GroupExpenseCreatedEvent event);

    List<BalanceResponse> getGroupBalances(UUID groupId);

    List<SettlementSuggestion> getSettlementPlan(UUID groupId);

    void recordSettlement(UUID groupId, UUID payerId, UUID payeeId, BigDecimal amount, String currency);
}

package com.expense.common.events;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GroupExpenseCreatedEvent {
    
    private UUID expenseId;
    private UUID groupId;
    private UUID paidBy;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;

    private List<ExpenseSplitPayload> splits;

    public static class ExpenseSplitPayload {
        private UUID userId;
        private BigDecimal amountOwed;

        public ExpenseSplitPayload() {}

        public ExpenseSplitPayload(UUID userId, BigDecimal amountOwed) {
            this.userId = userId;
            this.amountOwed = amountOwed;
        }

        public UUID getUserId() { return userId; }
        public BigDecimal getAmountOwed() { return amountOwed; }
    }

     public GroupExpenseCreatedEvent() {}

    public GroupExpenseCreatedEvent(
            UUID expenseId,
            UUID groupId,
            UUID paidBy,
            BigDecimal amount,
            String currency,
            LocalDate expenseDate,
            List<ExpenseSplitPayload> splits) {

        this.expenseId = expenseId;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.amount = amount;
        this.currency = currency;
        this.expenseDate = expenseDate;
        this.splits = splits;
    }

    public UUID getExpenseId() { return expenseId; }
    public UUID getGroupId() { return groupId; }
    public UUID getPaidBy() { return paidBy; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public List<ExpenseSplitPayload> getSplits() { return splits; }

}

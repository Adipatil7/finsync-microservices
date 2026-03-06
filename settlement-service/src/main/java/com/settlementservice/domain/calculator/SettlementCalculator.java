package com.settlementservice.domain.calculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.settlementservice.dto.SettlementSuggestion;
import com.settlementservice.entity.BalanceLedger;
import com.settlementservice.entity.Settlement;

@Component
public class SettlementCalculator {

    /**
     * Creates per-expense settlement rows from a Kafka event.
     * Each non-payer participant owes their split amount to the payer.
     */
    public List<Settlement> calculateSettlements(GroupExpenseCreatedEvent event) {

        List<Settlement> settlements = new ArrayList<>();
        UUID payer = event.getPaidBy();

        for (GroupExpenseCreatedEvent.ExpenseSplitPayload split : event.getSplits()) {
            if (!split.getUserId().equals(payer)) {
                Settlement settlement = new Settlement();
                settlement.setExpenseId(event.getExpenseId());
                settlement.setGroupId(event.getGroupId());
                settlement.setPaidBy(split.getUserId());
                settlement.setPaidTo(payer);
                settlement.setAmount(split.getAmountOwed());
                settlement.setCurrency(event.getCurrency());
                settlement.setSettlementDate(LocalDate.now());
                settlement.setCreatedAt(LocalDateTime.now());
                settlements.add(settlement);
            }
        }

        return settlements;
    }

    /**
     * Greedy debt simplification algorithm (Splitwise-style).
     *
     * Given all balance ledger entries for a group:
     * 1. Compute net balance per user (positive = creditor, negative = debtor)
     * 2. Use a greedy approach to match largest creditor with largest debtor
     * 3. Minimize total number of transactions
     *
     * Time complexity: O(n log n) where n = number of members
     */
    public List<SettlementSuggestion> computeSettlementPlan(List<BalanceLedger> balances, String defaultCurrency) {

        if (balances == null || balances.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 1: Compute net balance per user
        // net[user] = total amount others owe them - total amount they owe others
        Map<UUID, BigDecimal> netBalances = new HashMap<>();

        for (BalanceLedger ledger : balances) {
            // creditor is owed money (positive)
            netBalances.merge(ledger.getCreditorId(), ledger.getAmount(), BigDecimal::add);
            // debtor owes money (negative)
            netBalances.merge(ledger.getDebtorId(), ledger.getAmount().negate(), BigDecimal::add);
        }

        // Step 2: Separate into creditors (positive) and debtors (negative)
        // Use max-heap for creditors, max-heap (by absolute value) for debtors
        PriorityQueue<Map.Entry<UUID, BigDecimal>> creditors = new PriorityQueue<>(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );
        PriorityQueue<Map.Entry<UUID, BigDecimal>> debtors = new PriorityQueue<>(
                (a, b) -> a.getValue().compareTo(b.getValue()) // most negative first
        );

        for (Map.Entry<UUID, BigDecimal> entry : netBalances.entrySet()) {
            int cmp = entry.getValue().compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                creditors.add(entry);
            } else if (cmp < 0) {
                debtors.add(entry);
            }
            // skip users with zero net balance
        }

        // Step 3: Greedy matching — match largest creditor with largest debtor
        List<SettlementSuggestion> suggestions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<UUID, BigDecimal> creditor = creditors.poll();
            Map.Entry<UUID, BigDecimal> debtor = debtors.poll();

            BigDecimal creditAmount = creditor.getValue();
            BigDecimal debtAmount = debtor.getValue().abs();

            // Settle the minimum of the two
            BigDecimal settleAmount = creditAmount.min(debtAmount);

            suggestions.add(new SettlementSuggestion(
                    debtor.getKey(),    // from (debtor pays)
                    creditor.getKey(),  // to (creditor receives)
                    settleAmount,
                    defaultCurrency
            ));

            // Compute remaining balances
            BigDecimal remainingCredit = creditAmount.subtract(settleAmount);
            BigDecimal remainingDebt = debtAmount.subtract(settleAmount);

            // Re-insert if there's remaining balance
            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0) {
                creditor.setValue(remainingCredit);
                creditors.add(creditor);
            }
            if (remainingDebt.compareTo(BigDecimal.ZERO) > 0) {
                debtor.setValue(remainingDebt.negate());
                debtors.add(debtor);
            }
        }

        return suggestions;
    }
}

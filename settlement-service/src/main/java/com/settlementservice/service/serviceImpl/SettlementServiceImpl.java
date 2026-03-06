package com.settlementservice.service.serviceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.settlementservice.domain.calculator.SettlementCalculator;
import com.settlementservice.dto.BalanceResponse;
import com.settlementservice.dto.SettlementSuggestion;
import com.settlementservice.entity.BalanceLedger;
import com.settlementservice.entity.Settlement;
import com.settlementservice.repository.BalanceLedgerRepository;
import com.settlementservice.repository.SettlementRepository;
import com.settlementservice.service.SettlementService;

@Service
public class SettlementServiceImpl implements SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private BalanceLedgerRepository balanceLedgerRepository;

    @Autowired
    private SettlementCalculator settlementCalculator;

    @Override
    @Transactional
    public void processExpenseEvent(GroupExpenseCreatedEvent event) {

        // Idempotency check: skip if already processed
        if (settlementRepository.existsByExpenseId(event.getExpenseId())) {
            log.info("Expense {} already processed, skipping.", event.getExpenseId());
            return;
        }

        // 1. Save per-expense settlement rows
        List<Settlement> settlements = this.settlementCalculator.calculateSettlements(event);
        this.settlementRepository.saveAll(settlements);

        // 2. Update balance ledger
        UUID payer = event.getPaidBy();
        UUID groupId = event.getGroupId();
        String currency = event.getCurrency();

        for (GroupExpenseCreatedEvent.ExpenseSplitPayload split : event.getSplits()) {
            if (!split.getUserId().equals(payer)) {
                upsertBalanceLedger(groupId, split.getUserId(), payer, split.getAmountOwed(), currency);
            }
        }

        log.info("Processed expense {} for group {} — {} settlement rows, ledger updated.",
                event.getExpenseId(), groupId, settlements.size());
    }

    @Override
    public List<BalanceResponse> getGroupBalances(UUID groupId) {
        List<BalanceLedger> balances = balanceLedgerRepository.findByGroupId(groupId);
        return balances.stream()
                .map(b -> new BalanceResponse(b.getDebtorId(), b.getCreditorId(), b.getAmount(), b.getCurrency()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SettlementSuggestion> getSettlementPlan(UUID groupId) {
        List<BalanceLedger> balances = balanceLedgerRepository.findByGroupId(groupId);

        // Determine default currency from existing balances
        String currency = balances.isEmpty() ? "INR" : balances.get(0).getCurrency();

        return settlementCalculator.computeSettlementPlan(balances, currency);
    }

    @Override
    @Transactional
    public void recordSettlement(UUID groupId, UUID payerId, UUID payeeId, BigDecimal amount, String currency) {
        // When a user records a manual settlement (payer pays payee),
        // it reduces the debt: payer was the debtor, payee was the creditor.
        // So we subtract from the ledger entry where debtor=payer, creditor=payee.
        upsertBalanceLedger(groupId, payeeId, payerId, amount, currency);

        log.info("Recorded manual settlement in group {}: {} pays {} amount {}",
                groupId, payerId, payeeId, amount);
    }

    /**
     * Upsert the balance ledger between a debtor and creditor in a group.
     *
     * Logic:
     *   - If entry (debtor → creditor) exists: add amount
     *   - If reverse entry (creditor → debtor) exists: subtract; flip direction if needed
     *   - If neither exists: create new entry
     *   - If amount reaches zero: remove the entry
     */
    private void upsertBalanceLedger(UUID groupId, UUID debtorId, UUID creditorId,
                                      BigDecimal amount, String currency) {

        // Check if direct entry exists (debtor → creditor)
        Optional<BalanceLedger> directEntry = balanceLedgerRepository
                .findByGroupIdAndDebtorIdAndCreditorId(groupId, debtorId, creditorId);

        if (directEntry.isPresent()) {
            BalanceLedger ledger = directEntry.get();
            ledger.setAmount(ledger.getAmount().add(amount));
            balanceLedgerRepository.save(ledger);
            return;
        }

        // Check if reverse entry exists (creditor → debtor)
        Optional<BalanceLedger> reverseEntry = balanceLedgerRepository
                .findByGroupIdAndDebtorIdAndCreditorId(groupId, creditorId, debtorId);

        if (reverseEntry.isPresent()) {
            BalanceLedger ledger = reverseEntry.get();
            BigDecimal newAmount = ledger.getAmount().subtract(amount);

            int cmp = newAmount.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                // Direction stays the same, just reduce amount
                ledger.setAmount(newAmount);
                balanceLedgerRepository.save(ledger);
            } else if (cmp < 0) {
                // Direction flips: delete old entry, create new one
                balanceLedgerRepository.delete(ledger);
                BalanceLedger newLedger = new BalanceLedger();
                newLedger.setGroupId(groupId);
                newLedger.setDebtorId(debtorId);
                newLedger.setCreditorId(creditorId);
                newLedger.setAmount(newAmount.abs());
                newLedger.setCurrency(currency);
                balanceLedgerRepository.save(newLedger);
            } else {
                // Perfectly cancel out — remove the entry
                balanceLedgerRepository.delete(ledger);
            }
            return;
        }

        // No existing entry — create new
        BalanceLedger newLedger = new BalanceLedger();
        newLedger.setGroupId(groupId);
        newLedger.setDebtorId(debtorId);
        newLedger.setCreditorId(creditorId);
        newLedger.setAmount(amount);
        newLedger.setCurrency(currency);
        balanceLedgerRepository.save(newLedger);
    }
}

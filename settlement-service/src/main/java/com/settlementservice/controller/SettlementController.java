package com.settlementservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settlementservice.dto.BalanceResponse;
import com.settlementservice.dto.RecordSettlementRequest;
import com.settlementservice.dto.SettlementSuggestion;
import com.settlementservice.service.SettlementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    /**
     * Get all net balances for a group.
     * Returns who owes whom and how much.
     */
    @GetMapping("/groups/{groupId}/balances")
    public ResponseEntity<List<BalanceResponse>> getGroupBalances(@PathVariable UUID groupId) {
        List<BalanceResponse> balances = settlementService.getGroupBalances(groupId);
        return ResponseEntity.ok(balances);
    }

    /**
     * Get the optimized settlement plan for a group.
     * Uses greedy debt simplification to minimize the number of transactions.
     */
    @GetMapping("/groups/{groupId}/settlement-plan")
    public ResponseEntity<List<SettlementSuggestion>> getSettlementPlan(@PathVariable UUID groupId) {
        List<SettlementSuggestion> plan = settlementService.getSettlementPlan(groupId);
        return ResponseEntity.ok(plan);
    }

    /**
     * Record a manual settlement payment.
     * When a user pays another user, this reduces the balance between them.
     */
    @PostMapping("/groups/{groupId}/settle")
    public ResponseEntity<String> recordSettlement(
            @PathVariable UUID groupId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody RecordSettlementRequest request
    ) {
        settlementService.recordSettlement(
                groupId,
                request.getPayerId(),
                request.getPayeeId(),
                request.getAmount(),
                request.getCurrency(),
                requestingUserId
        );
        return ResponseEntity.ok("Settlement recorded successfully");
    }
}

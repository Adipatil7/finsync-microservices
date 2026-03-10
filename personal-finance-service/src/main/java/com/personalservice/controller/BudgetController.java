package com.personalservice.controller;

import com.personalservice.dto.BudgetRequest;
import com.personalservice.dto.BudgetResponse;
import com.personalservice.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody BudgetRequest request) {
        return new ResponseEntity<>(budgetService.createBudget(userId, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(budgetService.getBudgets(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.noContent().build();
    }
}

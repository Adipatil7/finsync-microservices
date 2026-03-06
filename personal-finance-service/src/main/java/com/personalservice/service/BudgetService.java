package com.personalservice.service;

import com.personalservice.dto.BudgetRequest;
import com.personalservice.dto.BudgetResponse;
import com.personalservice.entity.Budget;
import com.personalservice.entity.Transaction;
import com.personalservice.enums.TransactionType;
import com.personalservice.exception.ResourceNotFoundException;
import com.personalservice.exception.ValidationException;
import com.personalservice.mapper.BudgetMapper;
import com.personalservice.repository.BudgetRepository;
import com.personalservice.repository.CategoryRepository;
import com.personalservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    public BudgetResponse createBudget(UUID userId, BudgetRequest request) {
        if (request.getCategoryId() != null) {
            categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            
            if (budgetRepository.findByUserIdAndCategoryId(userId, request.getCategoryId()).isPresent()) {
                throw new ValidationException("Budget for this category already exists. Please update it instead.");
            }
        } else {
             if (budgetRepository.findByUserIdAndCategoryId(userId, null).isPresent()) {
                throw new ValidationException("Overall budget already exists. Please update it instead.");
            }
        }

        Budget budget = budgetMapper.toEntity(request);
        budget.setUserId(userId);
        budget = budgetRepository.save(budget);
        
        return populateBudgetDetails(budget, userId);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(UUID userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(b -> populateBudgetDetails(b, userId))
                .collect(Collectors.toList());
    }
    
    private BudgetResponse populateBudgetDetails(Budget budget, UUID userId) {
        BudgetResponse response = budgetMapper.toResponse(budget);
        
        if (budget.getCategoryId() != null) {
            categoryRepository.findById(budget.getCategoryId())
                    .ifPresent(c -> response.setCategoryName(c.getName()));
        } else {
            response.setCategoryName("Overall");
        }
        
        LocalDate now = LocalDate.now();
        LocalDate startOfPeriod;
        LocalDate endOfPeriod;
        
        switch (budget.getPeriod()) {
            case WEEKLY:
                startOfPeriod = now.minusDays(now.getDayOfWeek().getValue() - 1);
                endOfPeriod = startOfPeriod.plusDays(6);
                break;
            case YEARLY:
                startOfPeriod = now.withDayOfYear(1);
                endOfPeriod = now.withDayOfYear(now.lengthOfYear());
                break;
            case MONTHLY:
            default:
                startOfPeriod = now.withDayOfMonth(1);
                endOfPeriod = now.withDayOfMonth(now.lengthOfMonth());
                break;
        }
        
        List<Transaction> transactions;
        if (budget.getCategoryId() != null) {
            transactions = transactionRepository.findByUserIdAndCategoryIdAndDateBetween(
                    userId, budget.getCategoryId(), startOfPeriod, endOfPeriod);
        } else {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                    userId, startOfPeriod, endOfPeriod).stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .collect(Collectors.toList());
        }
        
        BigDecimal spent = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        response.setSpentAmount(spent);
        response.setRemainingAmount(budget.getLimitAmount().subtract(spent));
        
        return response;
    }
}

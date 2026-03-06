package com.personalservice.service;

import com.personalservice.dto.CalendarDayExpenseResponse;
import com.personalservice.dto.MonthlySummaryResponse;
import com.personalservice.dto.TransactionResponse;
import com.personalservice.entity.Category;
import com.personalservice.entity.Transaction;
import com.personalservice.enums.TransactionType;
import com.personalservice.mapper.TransactionMapper;
import com.personalservice.repository.CategoryRepository;
import com.personalservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(UUID userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month); // Format: YYYY-MM
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        Map<UUID, BigDecimal> categoryExpenses = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
                categoryExpenses.merge(t.getCategoryId(), t.getAmount(), BigDecimal::add);
            }
        }

        String topCategoryName = "None";
        BigDecimal topCategoryAmount = BigDecimal.ZERO;

        if (!categoryExpenses.isEmpty()) {
            Map.Entry<UUID, BigDecimal> maxEntry = Collections.max(categoryExpenses.entrySet(), Map.Entry.comparingByValue());
            topCategoryAmount = maxEntry.getValue();
            topCategoryName = categoryRepository.findById(maxEntry.getKey())
                    .map(Category::getName)
                    .orElse("Unknown");
        }

        return MonthlySummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(totalIncome.subtract(totalExpenses))
                .topCategoryName(topCategoryName)
                .topCategoryAmount(topCategoryAmount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CalendarDayExpenseResponse> getCalendarExpenses(UUID userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate)
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        Map<UUID, String> categoryNames = categoryRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<LocalDate, List<Transaction>> groupedByDate = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionDate));

        List<CalendarDayExpenseResponse> calendar = new ArrayList<>();
        
        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            LocalDate day = yearMonth.atDay(i);
            List<Transaction> dayTxs = groupedByDate.getOrDefault(day, Collections.emptyList());
            
            BigDecimal dayTotal = dayTxs.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            List<TransactionResponse> expenses = dayTxs.stream()
                    .map(t -> {
                        TransactionResponse res = transactionMapper.toResponse(t);
                        res.setCategoryName(categoryNames.getOrDefault(t.getCategoryId(), "Unknown"));
                        return res;
                    })
                    .collect(Collectors.toList());
                    
            calendar.add(new CalendarDayExpenseResponse(day, dayTotal, expenses));
        }

        return calendar;
    }
}

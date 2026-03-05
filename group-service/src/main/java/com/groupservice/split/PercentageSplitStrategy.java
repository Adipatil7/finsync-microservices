package com.groupservice.split;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.ExpenseSplitRequest;
import com.groupservice.entity.GroupExpenseSplit;

@Component
public class PercentageSplitStrategy implements SplitStrategy {

    @Override
    public List<GroupExpenseSplit> calculateSplits(
            CreateExpenseRequest request,
            UUID expenseId) {

        BigDecimal totalPercent = request.getSplits()
                .stream()
                .map(ExpenseSplitRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercent.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new RuntimeException("Percentages must equal 100");
        }

        List<GroupExpenseSplit> splits = new ArrayList<>();

        for (ExpenseSplitRequest split : request.getSplits()) {

            BigDecimal amount = request.getAmount()
                    .multiply(split.getAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            GroupExpenseSplit entity = new GroupExpenseSplit();
            entity.setExpenseId(expenseId);
            entity.setUserId(split.getUserId());
            entity.setAmountOwed(amount);
            entity.setCreatedAt(LocalDateTime.now());

            splits.add(entity);
        }

        return splits;
    }
}
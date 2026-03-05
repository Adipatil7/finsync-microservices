package com.groupservice.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.dto.ExpenseSplitRequest;
import com.groupservice.entity.GroupExpenseSplit;

@Component
public class ExactSplitStrategy implements SplitStrategy {

    @Override
    public List<GroupExpenseSplit> calculateSplits(CreateExpenseRequest request, UUID expenseId) {

        BigDecimal total = request.getSplits().stream()
                .map(ExpenseSplitRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("Total of splits must equal the expense amount");
        }

        List<GroupExpenseSplit> splits = new ArrayList<>();

        for (ExpenseSplitRequest split : request.getSplits()) {

            GroupExpenseSplit entity = new GroupExpenseSplit();
            entity.setExpenseId(expenseId);
            entity.setUserId(split.getUserId());
            entity.setAmountOwed(split.getAmount());
            entity.setCreatedAt(LocalDateTime.now());

            splits.add(entity);
        }

        return splits;


    }
    
}

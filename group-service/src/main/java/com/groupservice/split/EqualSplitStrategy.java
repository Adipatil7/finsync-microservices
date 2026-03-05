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
public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public List<GroupExpenseSplit> calculateSplits(CreateExpenseRequest request, UUID expenseId) {

        List<GroupExpenseSplit> splits = new ArrayList<>();

        BigDecimal total = request.getAmount();
        int users = request.getSplits().size();

        BigDecimal baseShare = total.divide(BigDecimal.valueOf(users), 2, RoundingMode.DOWN);

        BigDecimal allocated = baseShare.multiply(BigDecimal.valueOf(users));

        BigDecimal remainder = total.subtract(allocated);

        for (int i = 0; i < users; i++) {

            ExpenseSplitRequest split = request.getSplits().get(i);

            BigDecimal share = baseShare;

            if (i == users - 1) {
                share = share.add(remainder);
            }

            GroupExpenseSplit entity = new GroupExpenseSplit();
            entity.setExpenseId(expenseId);
            entity.setUserId(split.getUserId());
            entity.setAmountOwed(share);
            entity.setCreatedAt(LocalDateTime.now());

            splits.add(entity);

        }

        return splits;

    }

}

package com.groupservice.split;

import java.util.List;
import java.util.UUID;

import com.groupservice.dto.CreateExpenseRequest;
import com.groupservice.entity.GroupExpenseSplit;

public interface SplitStrategy {
    
    List<GroupExpenseSplit> calculateSplits(CreateExpenseRequest request,UUID expenseId);

}

package com.settlementservice.service;

import com.expense.common.events.GroupExpenseCreatedEvent;

public interface SettlementService {
    
    void processExpenseEvent(GroupExpenseCreatedEvent event);

}

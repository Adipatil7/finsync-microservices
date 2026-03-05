package com.settlementservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.expense.common.events.GroupExpenseCreatedEvent;
import com.settlementservice.service.SettlementService;

@Service
public class SettlementEventConsumer {
    
    @Autowired
    private SettlementService settlementService;

    // Method to consume settlement events from Kafka
    @KafkaListener(
        topics = "group-expense-created-topic",
        groupId = "settlement-service"
    )
    public void handleExpenseCreated(GroupExpenseCreatedEvent event){
        this.settlementService.processExpenseEvent(event);
    }

}

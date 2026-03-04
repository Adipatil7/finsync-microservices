package com.groupservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.expense.common.events.GroupExpenseCreatedEvent;

@Service
public class GroupEventProducer {
    
    @Autowired
    private KafkaTemplate<String, GroupExpenseCreatedEvent> kafkaTemplate;

    public void publishExpenseCreated(GroupExpenseCreatedEvent event){

        kafkaTemplate.send("group-expense-created-topic",event.getGroupId().toString(),event);

    }

}

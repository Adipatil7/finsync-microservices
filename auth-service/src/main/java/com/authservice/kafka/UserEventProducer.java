package com.authservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.expense.common.events.UserRegisteredEvent;

@Service
public class UserEventProducer {
    
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegisteredEvent(UserRegisteredEvent event){
        kafkaTemplate.send("user-registered-topic",event.getUserId().toString(),event);
    }

}

package com.groupservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.expense.common.events.UserRegisteredEvent;

@Service
public class UserEventConsumer {
    
    @KafkaListener(
        topics = "user-registered-topic",
        groupId = "group-service"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        System.out.println("Received UserRegisteredEvent: ");
        System.out.println("User ID: " + event.getUserId());
        System.out.println("Name: " + event.getName());
        System.out.println("Email: " + event.getEmail());
         // Here you can add logic to handle the event, such as updating your database or triggering other actions
    }

}

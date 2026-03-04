package com.groupservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.expense.common.events.UserRegisteredEvent;
import com.groupservice.entity.GroupUser;
import com.groupservice.repository.GroupUserRepository;

@Service
public class UserEventConsumer {

    @Autowired
    private GroupUserRepository groupUserRepository;
    
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
        GroupUser groupUser = new GroupUser();
        groupUser.setUserId(event.getUserId());
        groupUser.setName(event.getName());
        groupUser.setEmail(event.getEmail());
        this.groupUserRepository.save(groupUser);
    }

}

package com.authservice.Service.ServiceImpl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.authservice.Service.UserService;
import com.authservice.dto.RegisterUserRequest;
import com.authservice.entity.User;
import com.authservice.kafka.UserEventProducer;
import com.authservice.repository.UserRepository;
import com.expense.common.events.UserRegisteredEvent;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEventProducer userEventProducer;

    public User saveUser(RegisterUserRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        System.out.println("User saved successfully");
        UserRegisteredEvent event = new UserRegisteredEvent(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail()
        );

        System.out.println(savedUser.getId()+" "+savedUser.getName()+" "+savedUser.getEmail());
        
        // Publish the event to Kafka
        userEventProducer.publishUserRegisteredEvent(event);
        System.out.println("Event Published successfully");

        return savedUser;

    }

}

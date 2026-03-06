package com.personalservice.kafka;

import com.expense.common.events.UserRegisteredEvent;
import com.personalservice.entity.UserProjection;
import com.personalservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventKafkaListener {

    private final UserRepository userRepository;

    @KafkaListener(topics = "user-registered-topic", groupId = "personal-finance-group")
    @Transactional
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.getUserId());
        
        if (!userRepository.findByUserId(event.getUserId()).isPresent()) {
            UserProjection user = UserProjection.builder()
                    .userId(event.getUserId())
                    .email(event.getEmail())
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
            log.info("UserProjection saved successfully for user: {}", event.getUserId());
        } else {
            log.info("UserProjection already exists for user: {}", event.getUserId());
        }
    }
}

package com.authservice.kafka;

import com.expense.common.events.UserRegisteredEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate(
            org.springframework.kafka.core.ProducerFactory<String, UserRegisteredEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
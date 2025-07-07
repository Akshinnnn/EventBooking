package com.eventbooking.eventservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic EventUpdatedTopicCreation() {
        return TopicBuilder.name("EventUpdated").build();
    }

    @Bean
    public NewTopic EventCancelledTopicCreation() {
        return TopicBuilder.name("EventCancelled").build();
    }
}

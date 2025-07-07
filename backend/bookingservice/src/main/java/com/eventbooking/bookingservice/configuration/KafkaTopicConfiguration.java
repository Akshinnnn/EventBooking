package com.eventbooking.bookingservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic BookingCreatedTopicCreation() {
        return TopicBuilder.name("BookingCreated").build();
    }

    @Bean
    public NewTopic BookingCancelledTopicCreation() {
        return TopicBuilder.name("BookingCancelled").build();
    }

    @Bean
    public NewTopic EventBookingsCancelledTopicCreation() {
        return TopicBuilder.name("EventBookingsCancelled").build();
    }

    @Bean
    public NewTopic EventUpdatedBookingsTopicCreation() {
        return TopicBuilder.name("EventUpdatedBookings").build();
    }
}

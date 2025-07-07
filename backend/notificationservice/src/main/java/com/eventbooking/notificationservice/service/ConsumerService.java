package com.eventbooking.notificationservice.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.eventbooking.notificationservice.dto.BookingDTO;
import com.eventbooking.notificationservice.dto.EventDTO;
import com.eventbooking.notificationservice.dto.ReviewDTO;
import com.eventbooking.notificationservice.model.Notification;
import com.eventbooking.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private EventDTO getEventDetails(UUID eventID) {
        EventDTO event = webClientBuilder.build()
                .get()
                .uri("http://eventservice:8080/api/events/" + eventID)
                .retrieve()
                .bodyToMono(EventDTO.class)
                .block();

        return event;
    }

    @KafkaListener(topics = "BookingCreated", groupId = "NotificationServiceConsumer")
    public void processBooking(BookingDTO bookingDTO) {
        log.info("Log message - recieved from BookingCreated topic: {} ", bookingDTO.toString());

        Notification notification = new Notification();

        notification.setTitle("Booking Created");
        notification.setMessage("Your booking has been created successfully.");

        notification.setUserID(bookingDTO.getUserID());

        notification.setBookingID(bookingDTO.getId());

        notificationRepository.save(notification);

        log.info("Notification created for booking ID: {}", bookingDTO.getId());
    }

    @KafkaListener(topics = "BookingCancelled", groupId = "NotificationServiceConsumer")
    public void processBookingCancelled(BookingDTO bookingDTO) {
        log.info("Log message - recieved from BookingCancelled topic: {} ", bookingDTO.toString());

        Notification notification = new Notification();

        notification.setTitle("Booking Cancelled");
        notification.setMessage("Your booking has been cancelled. Your refund will be processed shortly.");

        notification.setUserID(bookingDTO.getUserID());

        notification.setBookingID(bookingDTO.getId());

        notificationRepository.save(notification);

        log.info("Notification created for booking ID: {}", bookingDTO.getId());
    }

    @KafkaListener(topics = "EventBookingsCancelled", groupId = "NotificationServiceConsumer")
    public void processEventBookingsCancelled(ArrayList<LinkedHashMap<String, Object>> bookingDTOs) {
        log.info("Log message - recieved from EventBookingsCancelled topic: {} ", bookingDTOs.toString());

        for (LinkedHashMap<String, Object> bookingDTOMap : bookingDTOs) {
            UUID bookingID = UUID.fromString(bookingDTOMap.get("id").toString());
            UUID userID = UUID.fromString(bookingDTOMap.get("userID").toString());

            Notification notification = new Notification();

            notification.setTitle("Booking cancelled because of event cancellation");
            notification.setMessage(
                    "Your booking has been cancelled because the event has been cancelled. Your refund will be processed shortly.");
            notification.setUserID(userID);
            notification.setBookingID(bookingID);
            notificationRepository.save(notification);

            log.info("Notification created for booking ID: {}", bookingID);
        }
    }

    @KafkaListener(topics = "EventUpdatedBookings", groupId = "NotificationServiceConsumer")
    public void processEventUpdatedBookings(ArrayList<LinkedHashMap<String, Object>> bookingDTOs) {
        log.info("Log message - recieved from EventUpdatedBookings topic: {} ", bookingDTOs.toString());

        UUID eventID = UUID.fromString(bookingDTOs.get(0).get("eventID").toString());

        List<UUID> userIDs = bookingDTOs.stream()
                .map(bookingDTOMap -> UUID.fromString(bookingDTOMap.get("userID").toString()))
                .distinct()
                .collect(Collectors.toList());

        for (UUID userID : userIDs) {
            Notification notification = new Notification();

            notification.setTitle("Event updated");
            notification.setMessage(
                    "An update has been made to the event you booked. Please check the event details for more information.");
            notification.setUserID(userID);
            notification.setEventID(eventID);
            notificationRepository.save(notification);

            log.info("Event update notification created for user ID: {}", userID);
        }
    }

    @KafkaListener(topics = "ReviewCreated", groupId = "NotificationServiceConsumer")
    public void processReview(ReviewDTO reviewDTO) {
        log.info("Log message - recieved from ReviewCreated topic: {} ", reviewDTO.toString());

        EventDTO event = null;

        try {
            event = getEventDetails(reviewDTO.getEventID());
            log.info("Event details retrieved: {}", event);
        } catch (Exception e) {
            log.error("Error retrieving event details: {}", e.getMessage());
            return;
        }

        Notification notification = new Notification();

        notification.setTitle("A new review has been created");
        notification.setMessage("A new review has been created for your event.");

        notification.setUserID(event.getOrganizerID());

        notification.setEventID(event.getId());

        notificationRepository.save(notification);

        log.info("Notification created for review ID: {}", reviewDTO.getId());
    }
}

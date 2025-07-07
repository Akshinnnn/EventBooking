package com.eventbooking.reviewservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.eventbooking.reviewservice.dto.BookingDTO;
import com.eventbooking.reviewservice.dto.EventDTO;
import com.eventbooking.reviewservice.dto.ReviewDTO;
import com.eventbooking.reviewservice.repository.ReviewRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationService {
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ReviewRepository reviewRepository;

    public EventDTO getEventDetails(UUID eventID) {
        EventDTO event = webClientBuilder
                .build()
                .get()
                .uri("http://eventservice:8080/api/events/{eventID}", eventID)
                .retrieve()
                .bodyToMono(EventDTO.class)
                .block();

        return event;
    }

    public boolean isUserBookingValid(String authHeader, UUID eventID) {
        List<BookingDTO> booking = webClientBuilder
                .build()
                .get()
                .uri("http://bookingservice:8080/api/account/bookings?eventID={eventID}", eventID)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToFlux(BookingDTO.class)
                .filter(bookingDTO -> bookingDTO.getEventID().equals(eventID))
                .collectList()
                .block();

        if (booking == null || booking.isEmpty()) {
            return false;
        }

        for (BookingDTO bookingDTO : booking) {
            if (bookingDTO.getEventID().equals(eventID) && bookingDTO.getStatus().equals("CONFIRMED")) {
                return true;
            }
        }

        return false;
    }

    public boolean isReviewValid(ReviewDTO reviewDTO, EventDTO event) {
        if (event.getStatus() != null && event.getStatus().equals("CANCELED"))
            throw new IllegalArgumentException("Cannot leave a review for a canceled event");

        if (event.getStartDateTime() != null && event.getStartDateTime().isAfter(java.time.LocalDateTime.now()))
            throw new IllegalArgumentException("Cannot leave a review for an event that has not started yet");

        if (reviewRepository.findByEventIDAndUserID(reviewDTO.getEventID(), reviewDTO.getUserID()).size() > 0)
            throw new IllegalArgumentException("You have already left a review for this event");

        if (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");

        if (reviewDTO.getTitle() == null || reviewDTO.getTitle().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty");

        if (reviewDTO.getDescription() == null || reviewDTO.getDescription().isEmpty())
            throw new IllegalArgumentException("Description cannot be empty");

        if (reviewDTO.getDescription().length() > 500)
            throw new IllegalArgumentException("Description cannot exceed 500 characters");

        if (reviewDTO.getTitle().length() > 100)
            throw new IllegalArgumentException("Title cannot exceed 100 characters");

        return true;
    }
};

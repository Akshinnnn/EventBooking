package com.eventbooking.bookingservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.eventbooking.bookingservice.dto.BookingDTO;
import com.eventbooking.bookingservice.dto.EventDTO;
import com.eventbooking.bookingservice.model.Booking;
import com.eventbooking.bookingservice.model.BookingStatus;
import com.eventbooking.bookingservice.repository.BookingRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

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

    public boolean isBookingValid(BookingDTO bookingDTO, EventDTO eventDTO, String authHeader) {
        if (bookingDTO.getFullName() == null || bookingDTO.getFullName().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (bookingDTO.getEmail() == null || bookingDTO.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!bookingDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email is not valid");
        }

        if (!eventDTO.getStatus().equals("ACTIVE")) {
            throw new IllegalArgumentException("Event is not active");
        }

        if (eventDTO.getCapacity() <= bookingRepository.getParticipantCount(eventDTO.getId())) {
            throw new IllegalArgumentException("Event is fully booked");
        }

        if (eventDTO.getStartDateTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("You cannot book an event that starts in less than 1 hour");
        }

        if (eventDTO.getOrganizerID().equals(bookingDTO.getUserID())) {
            throw new IllegalArgumentException("You cannot book your own event");
        }

        BigDecimal userBalance = getUserBalance(authHeader);

        if (bookingDTO.getPrice().compareTo(userBalance) > 0) {
            throw new IllegalArgumentException("You do not have enough balance to book this event");
        }

        return true;
    }

    public boolean canCancelBooking(BookingDTO booking, UUID userID) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        EventDTO event = getEventDetails(booking.getEventID());

        if (event.getStartDateTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("You cannot cancel an event that starts in less than 1 hour");
        }

        if (event.getStatus().equals("CANCELED")) {
            throw new IllegalArgumentException("Event is already canceled");
        }

        return true;
    }

    public boolean isOwner(UUID userID, UUID bookingID) {
        Optional<Booking> booking = bookingRepository.findById(bookingID);
        return booking.isPresent() && booking.get().getUserID().equals(userID);
    }

    public boolean isBookingPresent(UUID bookingID) {
        return bookingRepository.existsById(bookingID);
    }

    private BigDecimal getUserBalance(String authHeader) {
        return webClientBuilder
                .build()
                .get()
                .uri("http://paymentservice:8080/api/account/balance")
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .block();
    }
};

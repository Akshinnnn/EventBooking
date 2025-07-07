package com.eventbooking.bookingservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.eventbooking.bookingservice.dto.BookingDTO;
import com.eventbooking.bookingservice.dto.EventDTO;
import com.eventbooking.bookingservice.service.BookingService;
import com.eventbooking.bookingservice.service.ValidationService;
import com.eventbooking.bookingservice.util.JwtUtil;

@RestController
@RequestMapping("/api")
public class BookingController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ValidationService validationService;

    @GetMapping("/account/bookings")
    public ResponseEntity<?> getUserBookings(
            @RequestHeader("authorization") String authHeader,
            @RequestParam(required = false) UUID eventID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        List<BookingDTO> bookings = bookingService.getUserBookings(userID, eventID);

        return ResponseEntity.status(HttpStatus.OK).body(bookings);
    }

    @GetMapping("/events/{eventID}/bookings")
    public ResponseEntity<?> getEventBookings(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID eventID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        EventDTO event = null;

        try {
            event = validationService.getEventDetails(eventID);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!event.getOrganizerID().equals(userID) && !jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view this event's bookings");

        List<BookingDTO> bookings = bookingService.getEventBookings(eventID);
        return ResponseEntity.status(HttpStatus.OK).body(bookings);
    }

    @PostMapping("/events/{eventID}/bookings")
    public ResponseEntity<?> createEventBooking(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID eventID,
            @RequestBody BookingDTO bookingDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        EventDTO event = null;

        try {
            event = validationService.getEventDetails(eventID);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        UUID userID = jwtUtil.extractUserID(authHeader);

        bookingDTO.setUserID(userID);
        bookingDTO.setEventID(eventID);
        bookingDTO.setPrice(event.getPrice());

        try {
            validationService.isBookingValid(bookingDTO, event, authHeader);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid booking details: " + e.getMessage());
        }

        BookingDTO booking = bookingService.addBooking(bookingDTO, event, userID);

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/bookings/{bookingID}")
    public ResponseEntity<?> getBooking(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID bookingID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        BookingDTO booking = bookingService.getBooking(bookingID).orElse(null);

        if (booking == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");

        EventDTO event = null;

        try {
            event = validationService.getEventDetails(booking.getEventID());
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!booking.getUserID().equals(userID)
                && !event.getOrganizerID().equals(userID)
                && !jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to view this booking");

        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }

    @PutMapping("/bookings/{bookingID}/cancel")
    public ResponseEntity<?> cancelUserBooking(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID bookingID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        BookingDTO booking = bookingService.getBooking(bookingID).orElse(null);

        if (booking == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");

        EventDTO event = null;

        try {
            event = validationService.getEventDetails(booking.getEventID());
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!booking.getUserID().equals(userID)
                && !event.getOrganizerID().equals(userID)
                && !jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to cancel this booking");

        try {
            validationService.canCancelBooking(booking, userID);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid booking details: " + e.getMessage());
        }

        BookingDTO bookingDTO = bookingService.cancelBooking(bookingID);
        return ResponseEntity.status(HttpStatus.OK).body(bookingDTO);
    }
}

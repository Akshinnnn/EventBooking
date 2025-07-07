package com.eventbooking.bookingservice.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.eventbooking.bookingservice.dto.BookingDTO;
import com.eventbooking.bookingservice.dto.EventDTO;
import com.eventbooking.bookingservice.model.Booking;
import com.eventbooking.bookingservice.model.BookingStatus;
import com.eventbooking.bookingservice.repository.BookingRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, BookingDTO> kafkaTemplate;

    public List<BookingDTO> getEventBookings(UUID eventID) {
        log.info("Fetching all bookings for event: {}", eventID);
        return bookingRepository.findAll(Example.of(Booking.builder().eventID(eventID).build()))
                .stream()
                .map(this::mapToBookingDTO)
                .toList();
    }

    public List<BookingDTO> getUserBookings(UUID userID, UUID eventID) {
        log.info("Fetching all bookings for user: {}", userID);
        return bookingRepository
                .findAll(Example.of(Booking.builder().userID(userID).eventID(eventID).build()))
                .stream()
                .map(this::mapToBookingDTO)
                .toList();
    }

    public Optional<BookingDTO> getBooking(UUID id) {
        log.info("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id).map(this::mapToBookingDTO);
    }

    public BookingDTO addBooking(BookingDTO bookingDTO, EventDTO event, UUID userID) {
        Booking booking = Booking.builder()
                .fullName(bookingDTO.getFullName())
                .email(bookingDTO.getEmail())
                .build();

        // Set event details
        booking.setEventID(event.getId());

        // Set the user ID
        booking.setUserID(userID);

        // Set the booking status to CONFIRMED
        booking.setStatus(BookingStatus.PENDING);

        // Set the booking price
        booking.setPrice(event.getPrice());

        bookingRepository.save(booking);

        bookingDTO = mapToBookingDTO(booking);

        kafkaTemplate.send("BookingCreated", bookingDTO);

        log.info("Booking {} is added to the Database", booking.getId());

        return bookingDTO;
    }

    public BookingDTO cancelBooking(UUID id) {
        Booking booking = bookingRepository.findById(id).get();

        // Update the booking status to cancelled
        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        BookingDTO bookingDTO = mapToBookingDTO(booking);

        kafkaTemplate.send("BookingCancelled", bookingDTO);

        log.info("Booking {} is cancelled", booking.getId());

        return bookingDTO;
    }

    private BookingDTO mapToBookingDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .fullName(booking.getFullName())
                .email(booking.getEmail())
                .price(booking.getPrice())
                .userID(booking.getUserID())
                .eventID(booking.getEventID())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
};

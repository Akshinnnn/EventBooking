package com.eventbooking.bookingservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.eventbooking.bookingservice.dto.BookingDTO;
import com.eventbooking.bookingservice.dto.EventDTO;
import com.eventbooking.bookingservice.dto.PaymentDTO;
import com.eventbooking.bookingservice.model.Booking;
import com.eventbooking.bookingservice.model.BookingStatus;
import com.eventbooking.bookingservice.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, List<BookingDTO>> kafkaTemplate;

    @KafkaListener(topics = "PaymentCreated", groupId = "BookingServiceConsumer")
    public void processPayment(PaymentDTO paymentDTO) {
        log.info("Log message - recieved from PaymentCreated topic: {} ", paymentDTO.toString());

        Booking booking = bookingRepository.findById(paymentDTO.getBookingID()).orElse(null);

        if (booking == null) {
            log.error("Booking not found for ID: {}", paymentDTO.getBookingID());
            return;
        }

        if (paymentDTO.getStatus().equals("APPROVED")) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Booking confirmed for ID: {}", paymentDTO.getBookingID());
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            bookingRepository.save(booking);
            log.error("Booking cancelled for ID: {}", paymentDTO.getBookingID());
        }
    }

    @KafkaListener(topics = "EventCancelled", groupId = "BookingServiceConsumer")
    public void processEventCancelled(EventDTO eventDTO) {
        log.info("Log message - recieved from EventCancelled topic: {} ", eventDTO.toString());

        List<Booking> bookings = bookingRepository.getActiveEventBookings(eventDTO.getId());

        if (bookings.isEmpty()) {
            log.error("No bookings found for event ID: {}", eventDTO.getId());
            return;
        }

        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            log.info("Booking cancelled for ID: {}", booking.getId());
        }

        kafkaTemplate.send("EventBookingsCancelled", bookings.stream().map(this::mapToBookingDTO).toList());

        log.info("All bookings cancelled for event ID: {}", eventDTO.getId());
    }

    @KafkaListener(topics = "EventUpdated", groupId = "BookingServiceConsumer")
    public void processEventUpdate(EventDTO eventDTO) {
        log.info("Log message - recieved from EventUpdated topic: {} ", eventDTO.toString());

        List<Booking> bookings = bookingRepository.getActiveEventBookings(eventDTO.getId());

        if (bookings.isEmpty()) {
            log.error("No bookings found for event ID: {}", eventDTO.getId());
            return;
        }

        kafkaTemplate.send("EventUpdatedBookings", bookings.stream().map(this::mapToBookingDTO).toList());
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
}

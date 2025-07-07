package com.eventbooking.paymentservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.eventbooking.paymentservice.dto.BookingDTO;
import com.eventbooking.paymentservice.dto.PaymentDTO;
import com.eventbooking.paymentservice.model.Payment;
import com.eventbooking.paymentservice.model.PaymentStatus;
import com.eventbooking.paymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerService {
    @Autowired
    private PaymentRepository paymentRepository;

    private final KafkaTemplate<String, PaymentDTO> kafkaTemplate;

    @KafkaListener(topics = "BookingCreated", groupId = "PaymentServiceConsumer")
    public void processBooking(BookingDTO bookingDTO) {
        log.info("Log message - recieved from Booking Created topic: {} ", bookingDTO.toString());

        Payment payment = new Payment();

        payment.setAmount(bookingDTO.getPrice().negate());
        payment.setBookingID(bookingDTO.getId());
        payment.setUserID(bookingDTO.getUserID());

        BigDecimal balance = paymentRepository.getUserBalance(bookingDTO.getUserID());

        if (balance != null && balance.compareTo(bookingDTO.getPrice()) >= 0) {
            payment.setStatus(PaymentStatus.APPROVED);
            paymentRepository.save(payment);
            log.info("Payment successful for booking ID: {}", bookingDTO.getId());
        } else {
            payment.setStatus(PaymentStatus.REJECTED);
            paymentRepository.save(payment);
            log.error("Payment failed for booking ID: {}. Insufficient balance.", bookingDTO.getId());
        }

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(payment.getId());
        paymentDTO.setAmount(payment.getAmount());
        paymentDTO.setUserID(payment.getUserID());
        paymentDTO.setBookingID(payment.getBookingID());
        paymentDTO.setStatus(payment.getStatus());
        paymentDTO.setCreatedAt(payment.getCreatedAt());

        // Send payment status to the booking service
        kafkaTemplate.send("PaymentCreated", paymentDTO);

        log.info("Log message - sent to payment topic: {} ", paymentDTO.toString());
    }

    @KafkaListener(topics = "BookingCancelled", groupId = "PaymentServiceConsumer")
    public void processBookingCancellation(BookingDTO bookingDTO) {
        log.info("Log message - recieved from order topic: {} ", bookingDTO.toString());

        Payment payment = paymentRepository.findByBookingID(bookingDTO.getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                .findFirst()
                .orElse(null);

        if (payment != null && payment.getStatus() == PaymentStatus.APPROVED) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("Payment cancelled for booking ID: {}", bookingDTO.getId());
        } else {
            log.error("Payment not found or already cancelled for booking ID: {}", bookingDTO.getId());
        }
    }

    @KafkaListener(topics = "EventBookingsCancelled", groupId = "PaymentServiceConsumer")
    public void processEventBookingsCancelled(ArrayList<LinkedHashMap<String, Object>> bookingDTOs) {
        log.info("Log message - recieved from EventBookingsCancelled topic: {} ", bookingDTOs.toString());

        for (LinkedHashMap<String, Object> bookingDTOMap : bookingDTOs) {
            UUID bookingID = UUID.fromString(bookingDTOMap.get("id").toString());

            log.info("Booking ID: {}", bookingID);

            Payment payment = paymentRepository.findByBookingID(bookingID)
                    .stream()
                    .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                    .findFirst()
                    .orElse(null);

            if (payment != null && payment.getStatus() == PaymentStatus.APPROVED) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                log.info("Payment cancelled for booking ID: {}", bookingID);
            } else {
                log.error("Payment not found or already cancelled for booking ID: {}", bookingID);
            }
        }
    }
}

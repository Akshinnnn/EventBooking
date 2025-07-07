package com.eventbooking.paymentservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventbooking.paymentservice.dto.PaymentDTO;
import com.eventbooking.paymentservice.model.Payment;
import com.eventbooking.paymentservice.model.PaymentStatus;
import com.eventbooking.paymentservice.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public List<PaymentDTO> getUserPayments(UUID userID) {
        log.info("Fetching all payments for user: {}", userID);

        return paymentRepository.findByUserID(userID)
                .stream()
                .map(this::mapToPaymentDTO)
                .toList();
    }

    public BigDecimal getUserBalance(UUID userID) {
        return paymentRepository.getUserBalance(userID);
    }

    public PaymentDTO increaseBalance(UUID userID, BigDecimal amount) {
        log.info("Increasing balance for user: {}", userID);

        Payment payment = Payment.builder()
                .userID(userID)
                .amount(amount)
                .build();

        // Set the payment status to COMPLETED
        payment.setStatus(PaymentStatus.APPROVED);

        Payment savedPayment = paymentRepository.save(payment);

        return mapToPaymentDTO(savedPayment);
    }

    private PaymentDTO mapToPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .bookingID(payment.getBookingID())
                .userID(payment.getUserID())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
};

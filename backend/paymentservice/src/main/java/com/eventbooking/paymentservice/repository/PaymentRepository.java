package com.eventbooking.paymentservice.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eventbooking.paymentservice.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM payment WHERE userID = ?1 AND status = 'APPROVED'", nativeQuery = true)
    BigDecimal getUserBalance(UUID userId);

    List<Payment> findByUserID(UUID userId);

    List<Payment> findByBookingID(UUID bookingId);
}

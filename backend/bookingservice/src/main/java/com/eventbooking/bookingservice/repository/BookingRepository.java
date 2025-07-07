package com.eventbooking.bookingservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eventbooking.bookingservice.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query(value = "SELECT * FROM booking WHERE eventID = ?1 AND (status = 'CONFIRMED' OR status = 'PENDING')", nativeQuery = true)
    List<Booking> getActiveEventBookings(UUID eventID);

    @Query(value = "SELECT COALESCE(COUNT(booking_id), 0) FROM booking WHERE eventID = ?1 AND (status = 'CONFIRMED' OR status = 'PENDING')", nativeQuery = true)
    Long getParticipantCount(UUID eventID);
}

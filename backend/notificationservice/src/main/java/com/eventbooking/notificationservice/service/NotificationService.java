package com.eventbooking.notificationservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventbooking.notificationservice.dto.NotificationDTO;
import com.eventbooking.notificationservice.model.Notification;
import com.eventbooking.notificationservice.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<NotificationDTO> getUserNotifications(UUID userID) {
        log.info("Fetching all notifications for user: {}", userID);
        return notificationRepository.findByUserID(userID).stream()
                .map(this::mapToNotificationDTO)
                .toList();
    }

    private NotificationDTO mapToNotificationDTO(Notification booking) {
        return NotificationDTO.builder()
                .id(booking.getId())
                .title(booking.getTitle())
                .message(booking.getMessage())
                .userID(booking.getUserID())
                .eventID(booking.getEventID())
                .bookingID(booking.getBookingID())
                .paymentID(booking.getPaymentID())
                .createdAt(booking.getCreatedAt())
                .build();
    }
};

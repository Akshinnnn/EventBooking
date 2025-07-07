package com.eventbooking.notificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    @Id
    private UUID id;

    private String title;
    private String message;

    private UUID userID;

    private UUID eventID;
    private UUID bookingID;
    private UUID paymentID;

    private LocalDateTime createdAt;
}
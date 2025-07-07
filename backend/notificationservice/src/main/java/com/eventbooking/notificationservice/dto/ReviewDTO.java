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
public class ReviewDTO {
    @Id
    private UUID id;

    private int rating;
    private String title;
    private String description;

    private UUID userID;
    private String username;

    private UUID eventID;

    private LocalDateTime createdAt;
}
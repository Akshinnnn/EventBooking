package com.eventbooking.reviewservice.dto;

import java.math.BigDecimal;
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
public class BookingDTO {
    @Id
    private UUID id;

    private String fullName;
    private String email;

    private UUID userID;
    private UUID eventID;

    private BigDecimal price;

    private String status;

    private LocalDateTime createdAt;
}
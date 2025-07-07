package com.eventbooking.bookingservice.dto;

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
public class EventDTO {
    @Id
    private UUID id;

    private String title;
    private String description;

    private String category;

    private BigDecimal price;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    private LocalDateTime startDateTime;

    private Integer capacity;

    private String status;

    private UUID organizerID;

    private LocalDateTime createdAt;
}
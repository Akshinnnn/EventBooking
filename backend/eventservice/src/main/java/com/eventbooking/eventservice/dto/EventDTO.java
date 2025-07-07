package com.eventbooking.eventservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.eventbooking.eventservice.model.EventStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private UUID categoryID;
    private String categoryName;

    private BigDecimal price;

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    private LocalDateTime startDateTime;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private UUID organizerID;

    private LocalDateTime createdAt;
}
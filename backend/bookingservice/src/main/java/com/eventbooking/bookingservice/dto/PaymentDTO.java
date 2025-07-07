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
public class PaymentDTO {
    @Id
    private UUID id;

    private BigDecimal amount;

    private UUID userID;

    private UUID bookingID;

    private String status;

    private LocalDateTime createdAt;
}
package com.eventbooking.userservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.eventbooking.userservice.model.Role;

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
public class UserDTO {
    @Id
    private UUID id;

    private String name;

    private String email;
    private String phone;

    private String username;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
}
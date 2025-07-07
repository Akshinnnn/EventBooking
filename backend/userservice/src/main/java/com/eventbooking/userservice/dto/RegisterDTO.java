package com.eventbooking.userservice.dto;

import com.eventbooking.userservice.model.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    private String name;

    private String email;
    private String phone;

    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}

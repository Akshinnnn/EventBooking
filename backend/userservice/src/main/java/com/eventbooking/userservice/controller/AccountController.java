package com.eventbooking.userservice.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eventbooking.userservice.dto.RegisterDTO;
import com.eventbooking.userservice.dto.UserDTO;
import com.eventbooking.userservice.service.UserService;
import com.eventbooking.userservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api")
public class AccountController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @GetMapping("/account")
    public ResponseEntity<?> getAccount(@RequestHeader("authorization") String authHeader) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this action!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        return ResponseEntity.status(HttpStatus.OK).body(userService.getByUserID(userID));
    }

    @PutMapping("/account")
    public ResponseEntity<?> updateAccount(
            @RequestHeader("authorization") String authHeader,
            @RequestBody RegisterDTO userDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this action!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!userService.isUsernameAvailable(userDTO.getUsername(), Optional.of(userID))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists!");
        }

        UserDTO user = userService.updateUser(userID, userDTO);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}

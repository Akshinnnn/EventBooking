package com.eventbooking.userservice.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.eventbooking.userservice.jwt.JwtService;
import com.eventbooking.userservice.dto.LoginDTO;
import com.eventbooking.userservice.dto.RegisterDTO;
import com.eventbooking.userservice.service.UserService;
import com.eventbooking.userservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO credentatials) {
        log.info("Login - username: {} ", credentatials.getUsername());

        if (credentatials.getUsername() == null || credentatials.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password cannot be null!");
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                credentatials.getUsername(),
                credentatials.getPassword());

        try {
            authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password!");
        }

        log.info("User authenticated successfully: {} ", credentatials.getUsername());

        // Generate JWT token
        String token = jwtService.generateToken(credentatials.getUsername());

        log.info("TOKEN Generated: {} ", token.toString());

        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @GetMapping("/auth/authenticate")
    public ResponseEntity<?> authenticate(@RequestHeader("authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(jwtUtil.isAuthenticated(authHeader));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password cannot be null!");
        }

        if (!userService.isUsernameAvailable(userDTO.getUsername(), Optional.empty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists!");
        }

        userService.addUser(userDTO);

        String jwtToken = jwtService.generateToken(userDTO.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
    }
}

package com.eventbooking.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eventbooking.userservice.service.UserService;
import com.eventbooking.userservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("authorization") String authHeader) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this action!");

        if (!jwtUtil.isAuthorized(authHeader, "ADMIN"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be an admin to do this action!");

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsers());
    }
}

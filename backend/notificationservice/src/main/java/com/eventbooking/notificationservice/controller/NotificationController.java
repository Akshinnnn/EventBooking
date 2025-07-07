package com.eventbooking.notificationservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.notificationservice.dto.NotificationDTO;
import com.eventbooking.notificationservice.service.NotificationService;
import com.eventbooking.notificationservice.util.JwtUtil;

@RestController
@RequestMapping("/api")
public class NotificationController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/account/notifications")
    public ResponseEntity<?> getUserPayments(@RequestHeader("authorization") String authHeader) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        List<NotificationDTO> notifications = notificationService.getUserNotifications(userID);

        return ResponseEntity.status(HttpStatus.OK).body(notifications);
    }
}

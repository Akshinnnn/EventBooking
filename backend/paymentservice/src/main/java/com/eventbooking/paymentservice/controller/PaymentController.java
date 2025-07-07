package com.eventbooking.paymentservice.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.paymentservice.dto.PaymentDTO;
import com.eventbooking.paymentservice.service.PaymentService;
import com.eventbooking.paymentservice.util.JwtUtil;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class PaymentController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/account/payments")
    public ResponseEntity<?> getUserPayments(@RequestHeader("authorization") String authHeader) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        List<PaymentDTO> payments = paymentService.getUserPayments(userID);

        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }

    @GetMapping("/account/balance")
    public ResponseEntity<?> getBalance(@RequestHeader("authorization") String authHeader) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        BigDecimal balance = paymentService.getUserBalance(userID);

        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }

    @PostMapping("/account/balance")
    public ResponseEntity<?> increaseBalance(@RequestHeader("authorization") String authHeader,
            @RequestBody BigDecimal amount) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        PaymentDTO paymentDTO = paymentService.increaseBalance(userID, amount);

        return ResponseEntity.status(HttpStatus.OK).body(paymentDTO);
    }
}

package com.eventbooking.reviewservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.eventbooking.reviewservice.dto.ReviewDTO;
import com.eventbooking.reviewservice.dto.EventDTO;
import com.eventbooking.reviewservice.service.ReviewService;
import com.eventbooking.reviewservice.service.ValidationService;
import com.eventbooking.reviewservice.util.JwtUtil;

@RestController
@RequestMapping("/api")
public class ReviewController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ValidationService validationService;

    @GetMapping("/account/reviews")
    public ResponseEntity<?> getUserReviews(@RequestHeader("authorization") String authHeader) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        UUID userID = jwtUtil.extractUserID(authHeader);

        List<ReviewDTO> reviews = reviewService.getUserReviews(userID);

        return ResponseEntity.status(HttpStatus.OK).body(reviews);
    }

    @GetMapping("/events/{eventID}/reviews")
    public ResponseEntity<?> getEventReviews(@PathVariable UUID eventID) {
        try {
            validationService.getEventDetails(eventID);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        List<ReviewDTO> reviews = reviewService.getEventReviews(eventID);
        return ResponseEntity.status(HttpStatus.OK).body(reviews);
    }

    @PostMapping("/events/{eventID}/reviews")
    public ResponseEntity<?> createEventReview(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID eventID,
            @RequestBody ReviewDTO reviewDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        EventDTO event = null;

        try {
            event = validationService.getEventDetails(eventID);
        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");
        }

        UUID userID = jwtUtil.extractUserID(authHeader);

        try {
            if (!validationService.isUserBookingValid(authHeader, eventID))
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You must have a booking for this event to leave a review");

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You must have a booking for this event to leave a review");
        }

        try {
            validationService.isReviewValid(reviewDTO, event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid review details: " + e.getMessage());
        }

        String username = jwtUtil.extractUsername(authHeader);

        ReviewDTO review = reviewService.addReview(reviewDTO, eventID, userID, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/reviews/{reviewID}")
    public ResponseEntity<?> getReview(@PathVariable UUID reviewID) {
        ReviewDTO review = reviewService.getReview(reviewID).orElse(null);

        if (review == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");

        return ResponseEntity.status(HttpStatus.OK).body(review);
    }

    @DeleteMapping("/reviews/{reviewID}")
    public ResponseEntity<?> deleteReview(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID reviewID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        ReviewDTO review = reviewService.getReview(reviewID).orElse(null);

        if (review == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found");

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!review.getUserID().equals(userID) && !jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this review");

        reviewService.deleteReview(reviewID);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

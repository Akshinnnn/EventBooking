package com.eventbooking.reviewservice.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.eventbooking.reviewservice.dto.ReviewDTO;
import com.eventbooking.reviewservice.model.Review;
import com.eventbooking.reviewservice.repository.ReviewRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private KafkaTemplate<String, ReviewDTO> kafkaTemplate;

    public List<ReviewDTO> getEventReviews(UUID eventID) {
        log.info("Fetching all reviews for event: {}", eventID);
        return reviewRepository.findByEventID(eventID)
                .stream()
                .map(this::mapToReviewDTO)
                .toList();
    }

    public List<ReviewDTO> getUserReviews(UUID userID) {
        log.info("Fetching all reviews for user: {}", userID);
        return reviewRepository.findByUserID(userID)
                .stream()
                .map(this::mapToReviewDTO)
                .toList();
    }

    public Optional<ReviewDTO> getReview(UUID id) {
        log.info("Fetching review with ID: {}", id);
        return reviewRepository.findById(id).map(this::mapToReviewDTO);
    }

    public ReviewDTO addReview(ReviewDTO reviewDTO, UUID eventID, UUID userID, String username) {
        Review review = Review.builder()
                .rating(reviewDTO.getRating())
                .title(reviewDTO.getTitle())
                .description(reviewDTO.getDescription())
                .build();

        // Set event details
        review.setEventID(eventID);

        // Set the user ID
        review.setUserID(userID);

        // Set the user name
        review.setUsername(username);

        reviewRepository.save(review);

        reviewDTO = mapToReviewDTO(review);

        kafkaTemplate.send("ReviewCreated", reviewDTO);

        log.info("Review {} is added to the Database", review.getId());

        return reviewDTO;
    }

    public void deleteReview(UUID reviewID) {
        log.info("Deleting review with ID: {}", reviewID);
        reviewRepository.deleteById(reviewID);
    }

    private ReviewDTO mapToReviewDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .title(review.getTitle())
                .description(review.getDescription())
                .userID(review.getUserID())
                .username(review.getUsername())
                .eventID(review.getEventID())
                .createdAt(review.getCreatedAt())
                .build();
    }
};

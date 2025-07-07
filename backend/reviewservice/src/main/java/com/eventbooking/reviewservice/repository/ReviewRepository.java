package com.eventbooking.reviewservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eventbooking.reviewservice.model.Review;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @Query(value = "SELECT COALESCE(COUNT(review_id), 0) FROM review WHERE eventID = ?1 ", nativeQuery = true)
    Long getReviewCount(UUID eventID);

    @Query(value = "SELECT COALESCE(AVG(rating), 0) FROM review WHERE eventID = ?1 ", nativeQuery = true)
    Double getAverageRating(UUID eventID);

    List<Review> findByEventID(UUID eventID);

    List<Review> findByUserID(UUID userID);

    List<Review> findByEventIDAndUserID(UUID eventID, UUID userID);
}

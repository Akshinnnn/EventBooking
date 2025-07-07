package com.eventbooking.eventservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eventbooking.eventservice.model.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {
    /**
     * Fetches all events based on the given organizer ID, category ID, and search
     * string.
     *
     * @param organizerID the ID of the event organizer (can be null)
     * @param categoryID  the ID of the event category (can be null)
     * @param search      the search string to filter events by title or description
     *                    (can be null)
     * @return a list of events matching the criteria with sorting by start date
     *         descending
     */

    @Query("SELECT e FROM Event e WHERE (:organizerID IS NULL OR e.organizerID = :organizerID) " +
            "AND (:categoryID IS NULL OR e.categoryID = :categoryID) " +
            "AND (:search IS NULL OR (e.title LIKE %:search%)) " +
            "ORDER BY e.startDateTime DESC")
    List<Event> getEvents(UUID organizerID, UUID categoryID, String search);
}

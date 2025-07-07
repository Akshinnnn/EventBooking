package com.eventbooking.eventservice.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.eventservice.dto.EventDTO;
import com.eventbooking.eventservice.service.EventService;
import com.eventbooking.eventservice.service.ValidationService;
import com.eventbooking.eventservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class EventController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EventService eventService;

    @Autowired
    private ValidationService validationService;

    @GetMapping("/events")
    public ResponseEntity<?> getEvents(
            @RequestParam(required = false) UUID organizerID,
            @RequestParam(required = false) UUID categoryID,
            @RequestParam(required = false) String search) {
        List<EventDTO> events = eventService.getEvents(organizerID, categoryID, search);

        return ResponseEntity.status(HttpStatus.OK).body(events);
    }

    @GetMapping("/events/{eventID}")
    public ResponseEntity<?> getEvent(@PathVariable UUID eventID) {
        if (!validationService.isEventPresent(eventID))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");

        Optional<EventDTO> event = eventService.getEvent(eventID);

        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @PostMapping("/events")
    public ResponseEntity<?> addEvent(
            @RequestHeader("authorization") String authHeader,
            @RequestBody EventDTO eventDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        if (!jwtUtil.isAuthorized(authHeader, List.of("ORGANIZER", "ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to create an event");

        UUID userID = jwtUtil.extractUserID(authHeader);

        eventDTO.setOrganizerID(userID);

        try {
            validationService.isEventValid(eventDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        EventDTO event = eventService.addEvent(eventDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PutMapping("/events/{eventID}")
    public ResponseEntity<?> updateEvent(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID eventID,
            @RequestBody EventDTO eventDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        EventDTO event = eventService.getEvent(eventID).orElse(null);

        if (event == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!validationService.isOrganizer(userID, eventID) && !jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this event");

        try {
            validationService.isEventUpdateValid(event, eventDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        event = eventService.updateEvent(eventID, eventDTO);

        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @PutMapping("/events/{eventID}/cancel")
    public ResponseEntity<?> cancelEvent(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID eventID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        EventDTO event = eventService.getEvent(eventID).orElse(null);

        if (event == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found");

        UUID userID = jwtUtil.extractUserID(authHeader);

        if (!validationService.isOrganizer(userID, eventID) && !jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to cancel this event");

        try {
            validationService.canCancelEvent(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        eventService.cancelEvent(eventID);

        return ResponseEntity.status(HttpStatus.OK).body("Event cancelled successfully");
    }
}

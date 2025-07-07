package com.eventbooking.eventservice.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.eventbooking.eventservice.dto.EventDTO;
import com.eventbooking.eventservice.model.Event;
import com.eventbooking.eventservice.model.EventStatus;
import com.eventbooking.eventservice.repository.CategoryRepository;
import com.eventbooking.eventservice.repository.EventRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private KafkaTemplate<String, EventDTO> kafkaTemplate;

    public List<EventDTO> getEvents(UUID organizerID, UUID categoryID, String search) {
        log.info("Fetching all events with organizerID: {}, categoryID: {}, search: {}", organizerID, categoryID,
                search);
        return eventRepository.getEvents(organizerID, categoryID, search)
                .stream()
                .map(this::mapToEventDTO)
                .toList();
    }

    public Optional<EventDTO> getEvent(UUID eventID) {
        log.info("Fetching event with ID: {}", eventID);
        return eventRepository.findById(eventID)
                .map(this::mapToEventDTO);
    }

    public EventDTO addEvent(EventDTO eventDTO) {
        Event event = Event.builder()
                .title(eventDTO.getTitle())
                .description(eventDTO.getDescription())
                .categoryID(eventDTO.getCategoryID())
                .price(eventDTO.getPrice())
                .street(eventDTO.getStreet())
                .city(eventDTO.getCity())
                .state(eventDTO.getState())
                .zipCode(eventDTO.getZipCode())
                .country(eventDTO.getCountry())
                .startDateTime(eventDTO.getStartDateTime())
                .capacity(eventDTO.getCapacity())
                .organizerID(eventDTO.getOrganizerID())
                .build();

        // Set the status to ACTIVE
        event.setStatus(EventStatus.ACTIVE);

        eventRepository.save(event);

        log.info("Event {} is added to the Database", event.getId());

        return mapToEventDTO(event);
    }

    public EventDTO updateEvent(UUID eventID, EventDTO eventDTO) {
        Event event = eventRepository.findById(eventID).orElseThrow(null);

        if (eventDTO.getTitle() != null)
            event.setTitle(eventDTO.getTitle());

        if (eventDTO.getDescription() != null)
            event.setDescription(eventDTO.getDescription());

        if (eventDTO.getCategoryID() != null)
            event.setCategoryID(eventDTO.getCategoryID());

        if (eventDTO.getPrice() != null)
            event.setPrice(eventDTO.getPrice());

        if (eventDTO.getStreet() != null)
            event.setStreet(eventDTO.getStreet());

        if (eventDTO.getCity() != null)
            event.setCity(eventDTO.getCity());

        if (eventDTO.getState() != null)
            event.setState(eventDTO.getState());

        if (eventDTO.getZipCode() != null)
            event.setZipCode(eventDTO.getZipCode());

        if (eventDTO.getCountry() != null)
            event.setCountry(eventDTO.getCountry());

        if (eventDTO.getStartDateTime() != null)
            event.setStartDateTime(eventDTO.getStartDateTime());

        if (eventDTO.getCapacity() != null)
            event.setCapacity(eventDTO.getCapacity());

        eventRepository.save(event);

        EventDTO updatedEvent = mapToEventDTO(event);

        kafkaTemplate.send("EventUpdated", updatedEvent);

        log.info("Event {} is updated", event.getId());

        return updatedEvent;
    }

    public EventDTO cancelEvent(UUID eventID) {
        Event event = eventRepository.findById(eventID).get();

        // Update the event status to cancelled
        event.setStatus(EventStatus.CANCELLED);

        eventRepository.save(event);

        kafkaTemplate.send("EventCancelled", mapToEventDTO(event));

        log.info("Event {} is cancelled", event.getId());

        return mapToEventDTO(event);
    }

    private EventDTO mapToEventDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .categoryID(event.getCategoryID())
                .categoryName(categoryRepository.findById(event.getCategoryID())
                        .map(category -> category.getName())
                        .orElse(null))
                .price(event.getPrice())
                .street(event.getStreet())
                .city(event.getCity())
                .state(event.getState())
                .zipCode(event.getZipCode())
                .country(event.getCountry())
                .startDateTime(event.getStartDateTime())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .organizerID(event.getOrganizerID())
                .createdAt(event.getCreatedAt())
                .build();
    }
};

package com.eventbooking.eventservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventbooking.eventservice.dto.EventDTO;
import com.eventbooking.eventservice.model.Event;
import com.eventbooking.eventservice.model.EventStatus;
import com.eventbooking.eventservice.repository.CategoryRepository;
import com.eventbooking.eventservice.repository.EventRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public boolean isOrganizer(UUID userID, UUID eventID) {
        Optional<Event> event = eventRepository.findById(eventID);
        return event.isPresent() && event.get().getOrganizerID().equals(userID);
    }

    public boolean isEventPresent(UUID eventID) {
        return eventRepository.existsById(eventID);
    }

    public boolean isEventActive(UUID eventID) {
        return eventRepository.findById(eventID).get().getStatus() == EventStatus.ACTIVE;
    }

    public boolean isCategoryPresent(UUID categoryID) {
        return categoryRepository.existsById(categoryID);
    }

    public boolean isCategoryNamePresent(String name) {
        return categoryRepository.findByName(name) != null;
    }

    public boolean isEventValid(EventDTO eventDTO) {
        if (eventDTO.getTitle() == null || eventDTO.getTitle().isEmpty())
            throw new IllegalArgumentException("Event title cannot be null or empty");

        if (eventDTO.getDescription() == null || eventDTO.getDescription().isEmpty())
            throw new IllegalArgumentException("Event description cannot be null or empty");

        if (eventDTO.getCategoryID() == null)
            throw new IllegalArgumentException("Event category cannot be null");

        if (eventDTO.getPrice() == null)
            throw new IllegalArgumentException("Event price cannot be null");

        if (eventDTO.getStreet() == null || eventDTO.getStreet().isEmpty())
            throw new IllegalArgumentException("Event street cannot be null or empty");

        if (eventDTO.getCity() == null || eventDTO.getCity().isEmpty())
            throw new IllegalArgumentException("Event city cannot be null or empty");

        if (eventDTO.getState() == null || eventDTO.getState().isEmpty())
            throw new IllegalArgumentException("Event state cannot be null or empty");

        if (eventDTO.getZipCode() == null || eventDTO.getZipCode().isEmpty())
            throw new IllegalArgumentException("Event zip code cannot be null or empty");

        if (eventDTO.getCountry() == null || eventDTO.getCountry().isEmpty())
            throw new IllegalArgumentException("Event country cannot be null or empty");

        if (eventDTO.getCapacity() == null || eventDTO.getCapacity() <= 0)
            throw new IllegalArgumentException("Event capacity must be greater than 0");

        if (eventDTO.getStartDateTime() == null)
            throw new IllegalArgumentException("Event start date and time cannot be null");

        if (eventDTO.getStartDateTime().isBefore(LocalDateTime.now().plusDays(1)))
            throw new IllegalArgumentException("Event start date and time must be at least 1 day in the future");

        return true;
    }

    public boolean isEventUpdateValid(EventDTO event, EventDTO eventDTO) {
        if (event.getStatus() == EventStatus.CANCELLED)
            throw new IllegalArgumentException("Event is already cancelled");

        if (event.getStartDateTime().isBefore(LocalDateTime.now().plusDays(1)))
            throw new IllegalArgumentException("Event has already started. Cannot update");

        if (eventDTO.getTitle() != null && eventDTO.getTitle().isEmpty())
            throw new IllegalArgumentException("Event title cannot be empty");

        if (eventDTO.getDescription() != null && eventDTO.getDescription().isEmpty())
            throw new IllegalArgumentException("Event description cannot be empty");

        if (eventDTO.getCategoryID() != null && !isCategoryPresent(eventDTO.getCategoryID()))
            throw new IllegalArgumentException("Event category does not exist");

        if (eventDTO.getPrice() != null && eventDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Event price must be greater than 0");

        if (eventDTO.getStreet() != null && eventDTO.getStreet().isEmpty())
            throw new IllegalArgumentException("Event street cannot be empty");

        if (eventDTO.getCity() != null && eventDTO.getCity().isEmpty())
            throw new IllegalArgumentException("Event city cannot be empty");

        if (eventDTO.getState() != null && eventDTO.getState().isEmpty())
            throw new IllegalArgumentException("Event state cannot be empty");

        if (eventDTO.getZipCode() != null && eventDTO.getZipCode().isEmpty())
            throw new IllegalArgumentException("Event zip code cannot be empty");

        if (eventDTO.getCountry() != null && eventDTO.getCountry().isEmpty())
            throw new IllegalArgumentException("Event country cannot be empty");

        if (eventDTO.getCapacity() != null && eventDTO.getCapacity() <= 0)
            throw new IllegalArgumentException("Event capacity must be greater than 0");

        if (eventDTO.getStartDateTime() != null
                && eventDTO.getStartDateTime().isBefore(LocalDateTime.now().plusDays(1)))
            throw new IllegalArgumentException("Event start date and time must be at least 1 day in the future");

        if (eventDTO.getStartDateTime() != null && eventDTO.getStartDateTime().isBefore(event.getStartDateTime()))
            throw new IllegalArgumentException(
                    "Event start date and time cannot be before the current start date and time");

        if (eventDTO.getStartDateTime().isBefore(LocalDateTime.now().plusDays(1)))
            throw new IllegalArgumentException("Event start date and time must be at least 1 day in the future");

        return true;
    }

    public boolean canCancelEvent(EventDTO event) {
        if (event.getStatus() == EventStatus.CANCELLED)
            throw new IllegalArgumentException("Event is already cancelled");

        if (event.getStartDateTime().isBefore(LocalDateTime.now().plusDays(1)))
            throw new IllegalArgumentException("Event start date and time must be at least 1 day in the future");

        return true;
    }
}

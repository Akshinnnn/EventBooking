package com.eventbooking.eventservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eventbooking.eventservice.model.Category;
import com.eventbooking.eventservice.model.Event;
import com.eventbooking.eventservice.model.EventStatus;
import com.eventbooking.eventservice.repository.CategoryRepository;
import com.eventbooking.eventservice.repository.EventRepository;

@Configuration
public class DataInitializer {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (eventRepository.count() > 0) {
                return;
            }

            // Create categories
            UUID Category1ID = createCategory("CONCERT");
            UUID Category2ID = createCategory("THEATER");
            UUID Category3ID = createCategory("SPORTS");
            UUID Category4ID = createCategory("EXHIBITION");
            UUID Category5ID = createCategory("FESTIVAL");
            UUID Category6ID = createCategory("WORKSHOP");
            UUID Category7ID = createCategory("SEMINAR");
            UUID Category8ID = createCategory("CONFERENCE");
            UUID Category9ID = createCategory("MEETUP");
            UUID Category10ID = createCategory("OTHER");

            Event event1 = new Event();

            event1.setTitle("Event 1");
            event1.setDescription("Description for Event 1");

            event1.setCategoryID(Category1ID);

            event1.setStreet("123 Main St");
            event1.setCity("New York");
            event1.setState("NY");
            event1.setZipCode("10001");
            event1.setCountry("USA");

            event1.setPrice(BigDecimal.valueOf(50.00));

            event1.setCapacity(100);

            event1.setStartDateTime(LocalDateTime.of(2025, 5, 20, 18, 0));

            event1.setOrganizerID(UUID.fromString("1fae4f4a-a356-4430-b260-10bc61ffae15"));

            event1.setStatus(EventStatus.ACTIVE);

            eventRepository.save(event1);

            Event event2 = new Event();

            event2.setTitle("Event 2");
            event2.setDescription("Description for Event 2");

            event2.setCategoryID(Category6ID);

            event2.setStreet("123 Main St");
            event2.setCity("New York");
            event2.setState("NY");
            event2.setZipCode("10001");
            event2.setCountry("USA");

            event2.setPrice(BigDecimal.valueOf(100.00));

            event2.setCapacity(5);

            event2.setStartDateTime(LocalDateTime.of(2024, 6, 20, 15, 0));

            event2.setOrganizerID(UUID.fromString("1fae4f4a-a356-4430-b260-10bc61ffae15"));

            event2.setStatus(EventStatus.ACTIVE);

            eventRepository.save(event2);

            Event event3 = new Event();

            event3.setTitle("Event 3");
            event3.setDescription("Description for Event 3");

            event3.setCategoryID(Category6ID);

            event3.setStreet("123 Main St");
            event3.setCity("New York");
            event3.setState("NY");
            event3.setZipCode("10001");
            event3.setCountry("USA");

            event3.setPrice(BigDecimal.valueOf(500.00));

            event3.setCapacity(200);

            event3.setStartDateTime(LocalDateTime.of(2025, 6, 20, 15, 0));

            event3.setOrganizerID(UUID.fromString("2fae4f4a-a356-4430-b260-10bc61ffae15"));

            event3.setStatus(EventStatus.ACTIVE);

            eventRepository.save(event3);

            Event event4 = new Event();

            event4.setTitle("Event 4");
            event4.setDescription("Description for Event 4");

            event4.setCategoryID(Category4ID);

            event4.setStreet("123 Main St");
            event4.setCity("New York");
            event4.setState("NY");
            event4.setZipCode("10001");
            event4.setCountry("USA");

            event4.setPrice(BigDecimal.valueOf(20.00));

            event4.setCapacity(200);

            event4.setStartDateTime(LocalDateTime.of(2025, 6, 20, 15, 0));

            event4.setOrganizerID(UUID.fromString("2fae4f4a-a356-4430-b260-10bc61ffae15"));

            event4.setStatus(EventStatus.CANCELLED);

            eventRepository.save(event4);
        };
    }

    private UUID createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        categoryRepository.save(category);
        return category.getId();
    }
}

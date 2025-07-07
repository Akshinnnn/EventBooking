package com.eventbooking.userservice;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eventbooking.userservice.model.Role;
import com.eventbooking.userservice.model.User;
import com.eventbooking.userservice.repository.UserRepository;

@Configuration
public class DataInitializer {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User organizer1 = new User(UUID.fromString("1fae4f4a-a356-4430-b260-10bc61ffae15"));

            organizer1.setName("Organizer1");

            organizer1.setEmail("organizer1@example.com");
            organizer1.setPhone("123456789");

            organizer1.setUsername("ORGANIZER1");

            organizer1.setPassword(passwordEncoder.encode("ORGANIZER1"));

            organizer1.setRole(Role.ORGANIZER);

            userRepository.save(organizer1);

            User organizer2 = new User(UUID.fromString("2fae4f4a-a356-4430-b260-10bc61ffae15"));

            organizer2.setName("Organizer2");

            organizer2.setEmail("organizer2@example.com");
            organizer2.setPhone("123456789");

            organizer2.setUsername("ORGANIZER2");

            organizer2.setPassword(passwordEncoder.encode("ORGANIZER2"));

            organizer2.setRole(Role.ORGANIZER);

            userRepository.save(organizer2);

            User participant1 = new User();

            participant1.setName("Participant1");

            participant1.setEmail("participant1@example.com");
            participant1.setPhone("987654321");

            participant1.setUsername("PARTICIPANT1");

            participant1.setPassword(passwordEncoder.encode("PARTICIPANT1"));

            participant1.setRole(Role.PARTICIPANT);

            userRepository.save(participant1);

            User participant2 = new User();

            participant2.setName("Participant2");

            participant2.setEmail("participant2@example.com");
            participant2.setPhone("987654321");

            participant2.setUsername("PARTICIPANT2");

            participant2.setPassword(passwordEncoder.encode("PARTICIPANT2"));

            participant2.setRole(Role.PARTICIPANT);

            userRepository.save(participant2);

            User admin = new User();

            admin.setName("Admin");
            admin.setEmail("admin@example.com");
            admin.setPhone("123456789");

            admin.setUsername("ADMIN");

            admin.setPassword(passwordEncoder.encode("ADMIN"));

            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        };
    }
}

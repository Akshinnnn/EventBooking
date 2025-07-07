package com.eventbooking.userservice.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventbooking.userservice.dto.RegisterDTO;
import com.eventbooking.userservice.dto.UserDTO;
import com.eventbooking.userservice.model.Role;
import com.eventbooking.userservice.model.User;
import com.eventbooking.userservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDTO)
                .toList();
    }

    public UserDTO addUser(RegisterDTO userDTO) {
        Role role = userDTO.getRole();

        if (role == null || role == Role.ADMIN) {
            userDTO.setRole(Role.PARTICIPANT);
        }

        User user = User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole())
                .build();

        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(user);

        log.info("A new user added: {}", userDTO.toString());

        return mapToUserDTO(user);
    }

    public UserDTO updateUser(UUID userID, RegisterDTO userDTO) {
        User user = userRepository.findById(userID).orElseThrow(null);

        if (userDTO.getName() != null)
            user.setName(userDTO.getName());

        if (userDTO.getEmail() != null)
            user.setEmail(userDTO.getEmail());

        if (userDTO.getPhone() != null)
            user.setPhone(userDTO.getPhone());

        if (userDTO.getUsername() != null)
            user.setUsername(userDTO.getUsername());

        if (userDTO.getPassword() != null)
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if (userDTO.getRole() == Role.ADMIN)
            user.setRole(Role.PARTICIPANT);
        else if (userDTO.getRole() != null)
            user.setRole(userDTO.getRole());

        userRepository.save(user);

        log.info("User updated: {}", userDTO.toString());

        return mapToUserDTO(user);
    }

    public boolean isUsernameAvailable(String username, Optional<UUID> userID) {
        User existingUser = userRepository.findByUsername(username).orElse(null);

        if (existingUser != null) {
            if (userID.isPresent() && existingUser.getId().equals(userID.get())) {
                return true;
            }
            log.error("User with username {} already exists", username);
            return false;
        }
        return true;
    }

    public UserDTO getByUserID(UUID userID) {
        return userRepository.findById(userID).map(this::mapToUserDTO).orElse(null);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

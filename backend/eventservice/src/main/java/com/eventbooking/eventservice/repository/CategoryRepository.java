package com.eventbooking.eventservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventbooking.eventservice.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Category findByName(String name);
}

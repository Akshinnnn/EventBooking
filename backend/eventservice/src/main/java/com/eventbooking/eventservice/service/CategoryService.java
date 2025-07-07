package com.eventbooking.eventservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventbooking.eventservice.dto.CategoryDTO;
import com.eventbooking.eventservice.model.Category;
import com.eventbooking.eventservice.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getCategories() {
        log.info("Fetching all categories");

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToCategoryDTO)
                .toList();
    }

    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .build();

        categoryRepository.save(category);

        log.info("Category {} is added to the Database", category.getId());

        return mapToCategoryDTO(category);
    }

    public CategoryDTO updateCategory(UUID categoryID, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryID).orElseThrow(null);

        if (categoryDTO.getName() != null)
            category.setName(categoryDTO.getName());

        categoryRepository.save(category);

        CategoryDTO updatedCategory = mapToCategoryDTO(category);

        log.info("Category {} is updated", category.getId());

        return updatedCategory;
    }

    public void deleteCategory(UUID categoryID) {
        Category category = categoryRepository.findById(categoryID).get();
        categoryRepository.delete(category);

        log.info("Category {} is deleted", category.getId());
    }

    private CategoryDTO mapToCategoryDTO(Category event) {
        return CategoryDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .createdAt(event.getCreatedAt())
                .build();
    }
};

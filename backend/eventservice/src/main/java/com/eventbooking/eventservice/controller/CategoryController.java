package com.eventbooking.eventservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventbooking.eventservice.dto.CategoryDTO;
import com.eventbooking.eventservice.service.CategoryService;
import com.eventbooking.eventservice.service.ValidationService;
import com.eventbooking.eventservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class CategoryController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ValidationService validationService;

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<CategoryDTO> categorys = categoryService.getCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categorys);
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(
            @RequestHeader("authorization") String authHeader,
            @RequestBody CategoryDTO categoryDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        if (!jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an admin to do this operation!");

        if (categoryDTO.getName() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category name cannot be null!");

        if (validationService.isCategoryNamePresent(categoryDTO.getName()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category with this name already exists!");

        CategoryDTO category = categoryService.addCategory(categoryDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/categories/{categoryID}")
    public ResponseEntity<?> updateCategory(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID categoryID,
            @RequestBody CategoryDTO categoryDTO) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        if (!validationService.isCategoryPresent(categoryID))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");

        if (!jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an admin to do this operation!");

        CategoryDTO category = categoryService.updateCategory(categoryID, categoryDTO);

        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @DeleteMapping("/categories/{categoryID}")
    public ResponseEntity<?> deleteCategory(
            @RequestHeader("authorization") String authHeader,
            @PathVariable UUID categoryID) {
        if (!jwtUtil.isAuthenticated(authHeader))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to do this operation!");

        if (!validationService.isCategoryPresent(categoryID))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");

        if (!jwtUtil.isAuthorized(authHeader, List.of("ADMIN")))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an admin to do this operation!");

        categoryService.deleteCategory(categoryID);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Category deleted successfully");
    }
}

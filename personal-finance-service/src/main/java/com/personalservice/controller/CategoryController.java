package com.personalservice.controller;

import com.personalservice.dto.CategoryRequest;
import com.personalservice.dto.CategoryResponse;
import com.personalservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CategoryRequest request) {
        return new ResponseEntity<>(categoryService.createCategory(userId, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(categoryService.getCategories(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.noContent().build();
    }
}

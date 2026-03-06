package com.personalservice.service;

import com.personalservice.dto.CategoryRequest;
import com.personalservice.dto.CategoryResponse;
import com.personalservice.entity.Category;
import com.personalservice.exception.ValidationException;
import com.personalservice.mapper.CategoryMapper;
import com.personalservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(UUID userId, CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndType(request.getName(), userId, request.getType())) {
            throw new ValidationException("Category with this name and type already exists");
        }
        
        Category category = categoryMapper.toEntity(request);
        category.setUserId(userId);
        
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(UUID userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}

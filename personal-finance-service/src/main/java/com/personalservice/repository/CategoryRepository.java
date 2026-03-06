package com.personalservice.repository;

import com.personalservice.entity.Category;
import com.personalservice.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserId(UUID userId);
    List<Category> findByUserIdAndType(UUID userId, TransactionType type);
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByNameIgnoreCaseAndUserIdAndType(String name, UUID userId, TransactionType type);
}

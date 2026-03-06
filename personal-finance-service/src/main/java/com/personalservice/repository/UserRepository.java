package com.personalservice.repository;

import com.personalservice.entity.UserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserProjection, UUID> {
    Optional<UserProjection> findByUserId(UUID userId);
}

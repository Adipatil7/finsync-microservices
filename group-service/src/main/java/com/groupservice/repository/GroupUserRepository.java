package com.groupservice.repository;

import com.groupservice.entity.GroupUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupUserRepository extends JpaRepository<GroupUser,UUID> {
    
}

package com.groupservice.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "group_user",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"email"})
        }
)
public class GroupUser {
    
    @Id
    private UUID userId;

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;


}

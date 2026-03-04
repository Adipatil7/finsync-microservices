package com.groupservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "group_expense_splits",
       uniqueConstraints = {
            @UniqueConstraint(columnNames = {"expense_id", "user_id"})
       }
)
public class GroupExpenseSplit {
    
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount_owed", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountOwed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}

package com.settlementservice.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@Entity
@Table(name = "settlements",
      uniqueConstraints = @UniqueConstraint(columnNames = {"expense_id", "paid_to"})
)
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "paid_by", nullable = false)
    private UUID paidBy;

    @Column(name = "paid_to", nullable = false)
    private UUID paidTo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

package com.skillfactory.practice.entity;

import com.skillfactory.practice.enums.OperationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long operationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;

    @Column(nullable = false)
    @Min(value = 0, message = "Сумма операции не может быть отрицательной.")
    private BigDecimal amount;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;  // Время операции

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}

package com.skillfactory.practice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    @Min(value = 0, message = "Баланс не может быть отрицательным.")
    private BigDecimal balance;

    public Customer(BigDecimal balance) {
        this.balance = balance;
    }
}

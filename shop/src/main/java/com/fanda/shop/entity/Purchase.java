package com.fanda.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    @PrePersist
    protected void onCreate() {
        this.purchasedAt = LocalDateTime.now();
    }
}

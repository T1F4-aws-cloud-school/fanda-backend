package com.fanda.shop.entity;

import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String imageUrl;

    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;


}

package com.fanda.shop.entity;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;
    private String userId;

    @Enumerated(EnumType.STRING)
    private Role role;
}

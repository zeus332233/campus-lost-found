package com.example.demo.model;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private Long createdAt;
    private Long updatedAt;
}
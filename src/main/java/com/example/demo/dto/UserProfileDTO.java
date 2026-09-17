package com.example.demo.dto;

import lombok.Data;


@Data
public class UserProfileDTO {
    private String phone;
    private String email;

    private String password;
}
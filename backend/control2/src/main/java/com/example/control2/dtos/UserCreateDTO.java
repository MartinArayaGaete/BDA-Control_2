package com.example.control2.dtos;

import lombok.Data;

@Data
public class UserCreateDTO {
    private String username;
    private String password;

    // coordinates
    private double longitude;
    private double latitude;
}
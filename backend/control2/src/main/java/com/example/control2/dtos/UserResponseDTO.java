package com.example.control2.dtos;

import com.example.control2.enums.UserStatus;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private double longitude;
    private double latitude;
    private UserStatus status;
}
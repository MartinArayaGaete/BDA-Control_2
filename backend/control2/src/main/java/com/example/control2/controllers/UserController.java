package com.example.control2.controllers;

import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserCreateDTO dto) {
        UserResponseDTO response = userService.createUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> usersList = userService.getAllUsers();
        return ResponseEntity.ok(usersList);
    }
}
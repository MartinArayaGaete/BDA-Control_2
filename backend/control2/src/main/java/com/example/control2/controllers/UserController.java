package com.example.control2.controllers;

import com.example.control2.dtos.LoginResponseDTO;
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
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserCreateDTO dto) {
        UserResponseDTO response = userService.createUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET ALL USERS
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> usersList = userService.getAllUsers();

        if (usersList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usersList);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserCreateDTO loginDto) {
        try {
            // Reuse the create DTO because it includes username and password
            LoginResponseDTO response = userService.login(loginDto.getUsername(), loginDto.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // If login fails, return 401 with the error message
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/login")
    public ResponseEntity<String> loginHelp() {
        return ResponseEntity.ok("Use POST /api/users/login with JSON body.");
    }


}
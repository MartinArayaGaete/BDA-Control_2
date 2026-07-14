package com.example.control2.controllers;

import com.example.control2.dtos.LoginResponseDTO;
import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.entities.UserEntity;
import com.example.control2.security.JwtCookieService;
import com.example.control2.security.JwtTokenProvider;
import com.example.control2.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final JwtCookieService cookieService;

    public UserController(UserService userService,
                          JwtTokenProvider tokenProvider,
                          JwtCookieService cookieService) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.cookieService = cookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateDTO dto) {
        try {
            UserResponseDTO response = userService.createUser(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDTO> usersPage = userService.getAllUsers(page, size);

        if (usersPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(usersPage);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            UserResponseDTO user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponseDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserCreateDTO loginDto,
                                       HttpServletResponse response) {
        try {
            UserEntity user = userService.authenticate(loginDto.getUsername(), loginDto.getPassword());
            String token = tokenProvider.generateToken(user.getUsername());
            cookieService.addTokenCookie(response, token);

            LoginResponseDTO body = new LoginResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    "Login exitoso"
            );

            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        try {
            UserResponseDTO user = userService.getUserByUsername(authentication.getName());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        cookieService.clearTokenCookie(response);
        return ResponseEntity.ok("Logout exitoso");
    }

    @GetMapping("/login")
    public ResponseEntity<String> loginHelp() {
        return ResponseEntity.ok("Use POST /api/users/login with JSON body.");
    }
}
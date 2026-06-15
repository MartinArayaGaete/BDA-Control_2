package com.example.control2.controllers;

import com.example.control2.dtos.LoginResponseDTO;
import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.entities.UserEntity;
import com.example.control2.security.JwtService;
import com.example.control2.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

import java.time.Duration;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpiration))
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateDTO dto) {
        try {
            UserResponseDTO response = userService.createUser(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // username duplicado
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // GET ALL USERS
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

    // OBTENER USUARIO POR USERNAME
    // GET /api/users/username/{username}
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            UserResponseDTO user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    // OBTENER USUARIO POR ID
    // GET /api/users/{id}
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
    public ResponseEntity<?> loginUser(@RequestBody UserCreateDTO loginDto) {
        try {
            UserEntity user = userService.authenticate(loginDto.getUsername(), loginDto.getPassword());
            String token = jwtService.generateToken(user.getUsername());
            ResponseCookie cookie = buildAccessCookie(token);

            LoginResponseDTO response = new LoginResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    "Login exitoso"
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (RuntimeException e) {
            // si falla devuelve 401
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
    public ResponseEntity<String> logout() {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout exitoso");
    }

    @GetMapping("/login")
    public ResponseEntity<String> loginHelp() {
        return ResponseEntity.ok("Use POST /api/users/login with JSON body.");
    }


}
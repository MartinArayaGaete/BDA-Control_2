package com.example.control2.services;

import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.entities.UserEntity;
import com.example.control2.enums.UserStatus;
import com.example.control2.mappers.UserMapper;
import com.example.control2.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    // CREATE
    public UserResponseDTO createUser(UserCreateDTO dto) {
        // Mapping
        UserEntity newUser = userMapper.toEntity(dto);

        // El usuario nace siempre activo
        newUser.setStatus(UserStatus.ACTIVE);

        // Save in DB
        UserEntity savedUser = userRepository.save(newUser);

        // Return DTO
        return userMapper.toDTO(savedUser);
    }

    // GET ALL
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE) // Filtramos los eliminados
                .map(userMapper::toDTO)
                .toList();
    }

    // SAFE DELETE
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }
}
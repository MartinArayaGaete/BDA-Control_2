package com.example.control2.services;

import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.entities.UserEntity;
import com.example.control2.enums.UserStatus;
import com.example.control2.mappers.UserMapper;
import com.example.control2.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponseDTO createUser(UserCreateDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso.");
        }
        UserEntity entity = userMapper.toEntity(dto);
        entity.setStatus(UserStatus.ACTIVE);
        return userMapper.toDTO(userRepository.save(entity));
    }

    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        return userRepository.findByStatus(UserStatus.ACTIVE, PageRequest.of(page, size))
                .map(userMapper::toDTO);
    }

    public UserResponseDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public UserResponseDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado ID: " + id));
    }

    public UserEntity authenticate(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return user;
    }
}
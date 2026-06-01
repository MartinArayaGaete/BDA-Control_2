package com.example.control2.services;

import com.example.control2.dtos.LoginResponseDTO;
import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.entities.UserEntity;
import com.example.control2.enums.UserStatus;
import com.example.control2.mappers.UserMapper;
import com.example.control2.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    // CREAR
    public UserResponseDTO createUser(UserCreateDTO dto) {

        // Validar si el usuario ya existe
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso. Por favor, elige otro.");
        }

        // Mapeo de DTO a Entidad
        UserEntity newUser = userMapper.toEntity(dto);

        // El usuario nace siempre activo
        newUser.setStatus(UserStatus.ACTIVE);

        // Guardar en la base de datos
        UserEntity savedUser = userRepository.save(newUser);

        // Retornar DTO
        return userMapper.toDTO(savedUser);
    }

    // OBTENER TODOS LOS USUARIOS (Paginado)
    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Filtrado y paginado directo en la base de datos
        return userRepository.findByStatus(UserStatus.ACTIVE, pageable)
                .map(userMapper::toDTO);
    }

    // ELIMINACIÓN SEGURA (Lógica)
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Cambiamos el estado a eliminado en lugar de borrar el registro
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    // INICIO DE SESIÓN
    public LoginResponseDTO login(String username, String password) {
        // Buscar al usuario por nombre
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar contraseña
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Retornar datos básicos para la sesión
        return new LoginResponseDTO(user.getId(), user.getUsername(), "Login exitoso");
    }
}
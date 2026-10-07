package com.claudiopaulo.userapp.service.impl;

import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.dto.UserResponse;
import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.exception.ResourceNotFoundException;
import com.claudiopaulo.userapp.repository.UserRepository;
import com.claudiopaulo.userapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        log.info("Attempting to register new user: {}", request.getUsername());
        
        // Initial check
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();
        
        try {
            User savedUser = userRepository.save(user);
            log.info("User registered successfully: {} with ID: {}", savedUser.getUsername(), savedUser.getId());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            log.error("DataIntegrityViolationException during user registration for username: {}", 
                      request.getUsername(), e);
            
            if (e.getMessage().contains("uk_username") || e.getMessage().contains("username")) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (e.getMessage().contains("uk_email") || e.getMessage().contains("email")) {
                throw new IllegalArgumentException("Email already exists");
            }
            throw new IllegalArgumentException("Error registering user: duplicate data");
        }
    }
    
    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
        
        log.debug("User found: {}", user.getUsername());
        return mapToResponse(user);
    }
    
    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        
        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} users", users.size());
        return users;
    }
    
    @Override
    @Transactional
    public UserResponse updateUser(Long id, RegisterRequest request) {
        log.info("Attempting to update user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
        
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        
        // Uniqueness checks
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            log.warn("Update failed: Username {} already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            log.warn("Update failed: Email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.debug("Password updated for user: {}", user.getUsername());
        }
        
        try {
            User updatedUser = userRepository.save(user);
            log.info("User updated successfully - ID: {}, Old username: {}, New username: {}, Old email: {}, New email: {}", 
                     id, oldUsername, updatedUser.getUsername(), oldEmail, updatedUser.getEmail());
            return mapToResponse(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("DataIntegrityViolationException during user update for ID: {}", id, e);
            
            if (e.getMessage().contains("uk_username") || e.getMessage().contains("username")) {
                throw new IllegalArgumentException("Username already exists");
            }
            if (e.getMessage().contains("uk_email") || e.getMessage().contains("email")) {
                throw new IllegalArgumentException("Email already exists");
            }
            throw new IllegalArgumentException("Error updating user: duplicate data");
        }
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Attempting to delete user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            log.warn("Delete failed: User not found with ID: {}", id);
            throw new ResourceNotFoundException("User not found");
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }
    
    @Override
    public User findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new ResourceNotFoundException("User not found");
                });
    }
    
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
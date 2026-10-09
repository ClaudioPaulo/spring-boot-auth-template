package com.claudiopaulo.userapp.integration;

import com.claudiopaulo.userapp.dto.LoginRequest;
import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should register new user successfully")
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
    
    @Test
    @DisplayName("Should fail registration with duplicate username")
    void testRegister_DuplicateUsername() throws Exception {
        // Given
        User existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(existingUser);
        
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("newemail@example.com");
        request.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
    
    @Test
    @DisplayName("Should fail registration with invalid email")
    void testRegister_InvalidEmail() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("invalid-email");
        request.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email"));
    }
    
    @Test
    @DisplayName("Should fail registration with short password")
    void testRegister_ShortPassword() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value(containsString("at least 6 characters")));
    }
    
    @Test
    @DisplayName("Should login successfully")
    void testLogin_Success() throws Exception {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
    
    @Test
    @DisplayName("Should fail login with wrong password")
    void testLogin_WrongPassword() throws Exception {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(user);
        
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
    
    @Test
    @DisplayName("Should fail login with non-existent user")
    void testLogin_NonExistentUser() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Should fail login with empty username")
    void testLogin_EmptyUsername() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));
    }
    
    @Test
    @DisplayName("Should fail login with empty password")
    void testLogin_EmptyPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is required"));
    }
}
package com.claudiopaulo.userapp.integration;

import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.repository.UserRepository;
import com.claudiopaulo.userapp.service.JwtService;
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
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    private String adminToken;
    private String userToken;
    private User adminUser;
    private User regularUser;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create admin user
        adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);
        
        // Create regular user
        regularUser = User.builder()
                .username("user")
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .enabled(true)
                .build();
        regularUser = userRepository.save(regularUser);
        userToken = jwtService.generateToken(regularUser);
    }
    
    @Test
    @DisplayName("Should get all users as admin")
    void testGetAllUsers_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("user"));
    }
    
    @Test
    @DisplayName("Should deny access to get all users as regular user")
    void testGetAllUsers_AsUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should deny access without authentication")
    void testGetAllUsers_NoAuth_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should get user by id as admin")
    void testGetUserById_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regularUser.getId()))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }
    
    @Test
    @DisplayName("Should get own user data as regular user")
    void testGetUserById_OwnData_Success() throws Exception {
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }
    
    @Test
    @DisplayName("Should deny access to other user data as regular user")
    void testGetUserById_OtherUserData_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent user")
    void testGetUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
    
    @Test
    @DisplayName("Should update own user data")
    void testUpdateUser_OwnData_Success() throws Exception {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("newpassword123");
        
        // When & Then
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
    
    @Test
    @DisplayName("Should update any user as admin")
    void testUpdateUser_AsAdmin_Success() throws Exception {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("adminupdated");
        updateRequest.setEmail("adminupdated@example.com");
        updateRequest.setPassword("newpass123");
        
        // When & Then
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminupdated"));
    }
    
    @Test
    @DisplayName("Should deny update of other user data")
    void testUpdateUser_OtherUserData_Forbidden() throws Exception {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("hacker");
        updateRequest.setEmail("hacker@example.com");
        updateRequest.setPassword("hack123");
        
        // When & Then
        mockMvc.perform(put("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should fail update with duplicate username")
    void testUpdateUser_DuplicateUsername_BadRequest() throws Exception {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("admin"); // Already exists
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPassword("password123");
        
        // When & Then
        mockMvc.perform(put("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
    
    @Test
    @DisplayName("Should delete user as admin")
    void testDeleteUser_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        
        // Verify user was deleted
        mockMvc.perform(get("/api/users/" + regularUser.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should deny delete as regular user")
    void testDeleteUser_AsUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/" + adminUser.getId())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 404 when deleting non-existent user")
    void testDeleteUser_NotFound() throws Exception {
        mockMvc.perform(delete("/api/users/999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
    
    @Test
    @DisplayName("Should deny delete without authentication")
    void testDeleteUser_NoAuth_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/" + regularUser.getId()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should deny access with invalid token")
    void testAccessWithInvalidToken_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should deny access with malformed token")
    void testAccessWithMalformedToken_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "InvalidFormat"))
                .andExpect(status().isForbidden());
    }
}
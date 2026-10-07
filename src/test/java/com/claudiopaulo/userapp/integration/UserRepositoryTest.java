package com.claudiopaulo.userapp.integration;

import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .enabled(true)
                .build();
    }
    
    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername_Success() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("Should return empty when username not found")
    void testFindByUsername_NotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail_Success() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();
        
        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("Should check if username exists")
    void testExistsByUsername() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();
        
        // When
        boolean exists = userRepository.existsByUsername("testuser");
        boolean notExists = userRepository.existsByUsername("other");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Given
        entityManager.persist(testUser);
        entityManager.flush();
        
        // When
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("other@example.com");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    @DisplayName("Should save user with timestamps")
    void testSaveUser_WithTimestamps() {
        // When
        User saved = userRepository.save(testUser);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isCloseTo(saved.getUpdatedAt(), within(1, ChronoUnit.MILLIS));
    }
    
    @Test
    @DisplayName("Should update user and change updatedAt timestamp")
    void testUpdateUser_UpdatesTimestamp() throws InterruptedException {
        // Given
        User saved = userRepository.save(testUser);
        entityManager.flush();
        
        Thread.sleep(100); // Small delay to ensure timestamp difference
        
        // When
        saved.setEmail("newemail@example.com");
        User updated = userRepository.save(saved);
        entityManager.flush();
        
        // Then
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }
}

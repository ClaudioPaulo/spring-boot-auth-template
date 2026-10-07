package com.claudiopaulo.userapp.unit.security;

import com.claudiopaulo.userapp.config.UserSecurity;
import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Security Unit Tests")
class UserSecurityTest {
    
    private UserSecurity userSecurity;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        userSecurity = new UserSecurity();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();
        
        SecurityContextHolder.clearContext();
    }
    
    @Test
    @DisplayName("Should return true when user is owner")
    void testIsOwner_UserIsOwner() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // When
        boolean isOwner = userSecurity.isOwner(1L);
        
        // Then
        assertThat(isOwner).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when user is not owner")
    void testIsOwner_UserIsNotOwner() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // When
        boolean isOwner = userSecurity.isOwner(2L);
        
        // Then
        assertThat(isOwner).isFalse();
    }
    
    @Test
    @DisplayName("Should return false when no authentication")
    void testIsOwner_NoAuthentication() {
        // Given - No authentication set
        
        // When
        boolean isOwner = userSecurity.isOwner(1L);
        
        // Then
        assertThat(isOwner).isFalse();
    }
}

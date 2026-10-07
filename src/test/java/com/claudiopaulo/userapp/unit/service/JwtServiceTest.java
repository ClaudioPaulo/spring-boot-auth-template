package com.claudiopaulo.userapp.unit.service;

import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT Service Unit Tests")
class JwtServiceTest {
    
    private JwtService jwtService;
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private UserDetails testUser;
    
    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inject values via reflection for testing
        try {
            var secretField = JwtService.class.getDeclaredField("secretKey");
            secretField.setAccessible(true);
            secretField.set(jwtService, secretKey);
            
            var expirationField = JwtService.class.getDeclaredField("jwtExpiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtService, jwtExpiration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();
    }
    
    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken_Success() {
        // When
        String token = jwtService.generateToken(testUser);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }
    
    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername_Success() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        String username = jwtService.extractUsername(token);
        
        // Then
        assertThat(username).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("Should validate token successfully")
    void testIsTokenValid_Success() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("Should invalidate token with wrong user")
    void testIsTokenValid_WrongUser() {
        // Given
        String token = jwtService.generateToken(testUser);
        UserDetails wrongUser = User.builder()
                .username("wronguser")
                .email("wrong@example.com")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .build();
        
        // When
        boolean isValid = jwtService.isTokenValid(token, wrongUser);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should detect expired token")
    void testIsTokenValid_ExpiredToken() {
        // Given - Create an expired token
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        String expiredToken = Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(testUser.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000)) // Expired 5 seconds ago
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        // When
        boolean isValid = jwtService.isTokenValid(expiredToken, testUser);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should generate token with extra claims")
    void testGenerateTokenWithClaims_Success() {
        // Given
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("email", "test@example.com");
        
        // When
        String token = jwtService.generateToken(extraClaims, testUser);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
    }
}

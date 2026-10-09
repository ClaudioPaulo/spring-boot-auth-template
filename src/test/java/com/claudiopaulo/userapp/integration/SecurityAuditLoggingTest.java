package com.claudiopaulo.userapp.integration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.claudiopaulo.userapp.dto.LoginRequest;
import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.entity.Role;
import com.claudiopaulo.userapp.entity.User;
import com.claudiopaulo.userapp.repository.UserRepository;
import com.claudiopaulo.userapp.security.SecurityAuditLogger;
import com.claudiopaulo.userapp.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security Audit Logging Tests")
class SecurityAuditLoggingTest {

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

    private Logger auditLogger;
    private Level previousLevel;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        auditLogger = (Logger) LoggerFactory.getLogger(SecurityAuditLogger.class);
        previousLevel = auditLogger.getLevel();
        auditLogger.setLevel(Level.INFO);
        appender = new ListAppender<>();
        appender.start();
        auditLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        auditLogger.detachAppender(appender);
        auditLogger.setLevel(previousLevel);
    }

    private boolean logged(String text) {
        return appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(text));
    }

    private User saveUser(String username, Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(role)
                .enabled(true)
                .build());
    }

    @Test
    @DisplayName("Should audit a registration")
    void registrationIsAudited() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("audited");
        request.setEmail("audited@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertTrue(logged("New user registration"));
    }

    @Test
    @DisplayName("Should audit successful and failed logins")
    void loginsAreAudited() throws Exception {
        saveUser("loginuser", Role.USER);

        LoginRequest good = new LoginRequest();
        good.setUsername("loginuser");
        good.setPassword("password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(good)))
                .andExpect(status().isOk());

        LoginRequest bad = new LoginRequest();
        bad.setUsername("loginuser");
        bad.setPassword("wrong-password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());

        assertTrue(logged("Successful login"));
        assertTrue(logged("Failed login attempt"));
    }

    @Test
    @DisplayName("Should audit a denied access attempt")
    void deniedAccessIsAudited() throws Exception {
        User user = saveUser("regular", Role.USER);

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + jwtService.generateToken(user)))
                .andExpect(status().isForbidden());

        assertTrue(logged("Unauthorized access attempt"));
    }

    @Test
    @DisplayName("Should audit a user deletion by an admin")
    void deletionIsAudited() throws Exception {
        User admin = saveUser("boss", Role.ADMIN);
        User target = saveUser("target", Role.USER);

        mockMvc.perform(delete("/api/users/" + target.getId())
                .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isNoContent());

        assertTrue(logged("User deletion"));
    }
}

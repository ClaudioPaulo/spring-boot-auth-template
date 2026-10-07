package com.claudiopaulo.userapp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "springdoc.swagger-ui.enabled=true",
    "springdoc.api-docs.enabled=true"
})
@DisplayName("Swagger Enabled Tests")
class SwaggerEnabledTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Should allow access to Swagger UI when enabled")
    void testSwaggerUiAccessWhenEnabled() throws Exception {
        // When & Then
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("Should allow access to API docs when enabled")
    void testApiDocsAccessWhenEnabled() throws Exception {
        // When & Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.openapi").value("3.1.0"))
                .andExpect(jsonPath("$.info.title").value("User Management API"));
    }
    
    @Test
    @DisplayName("Should have JWT security scheme in API docs")
    void testApiDocsHasJwtSecurity() throws Exception {
        // When & Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
    }
    
    @Test
    @DisplayName("Should document all authentication endpoints")
    void testApiDocsHasAuthEndpoints() throws Exception {
        // When & Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./api/auth/register").exists())
                .andExpect(jsonPath("$.paths./api/auth/login").exists());
    }
    
    @Test
    @DisplayName("Should document all user management endpoints")
    void testApiDocsHasUserEndpoints() throws Exception {
        // When & Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./api/users").exists())
                .andExpect(jsonPath("$.paths./api/users/{id}").exists());
    }
}
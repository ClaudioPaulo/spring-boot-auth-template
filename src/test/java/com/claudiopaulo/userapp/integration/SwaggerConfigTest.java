package com.claudiopaulo.userapp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Swagger Configuration Tests")
class SwaggerConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Should deny access to Swagger UI when disabled")
    void testSwaggerUiAccessWhenDisabled() throws Exception {
        // Given - Swagger is disabled in test profile
        
        // When & Then
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should deny access to API docs when disabled")
    void testApiDocsAccessWhenDisabled() throws Exception {
        // Given - Swagger is disabled in test profile
        
        // When & Then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is4xxClientError());
    }
}
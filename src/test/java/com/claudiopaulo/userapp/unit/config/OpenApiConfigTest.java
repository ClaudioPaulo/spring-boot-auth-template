package com.claudiopaulo.userapp.unit.config;

import com.claudiopaulo.userapp.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OpenAPI Configuration Unit Tests")
class OpenApiConfigTest {
    
    private OpenApiConfig openApiConfig;
    
    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        ReflectionTestUtils.setField(openApiConfig, "appVersion", "1.0.0");
        ReflectionTestUtils.setField(openApiConfig, "serverPort", "8080");
    }
    
    @Test
    @DisplayName("Should create OpenAPI configuration with correct info")
    void testCustomOpenAPI_HasCorrectInfo() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Then
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("User Management API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription()).contains("REST API for user management");
    }
    
    @Test
    @DisplayName("Should have contact information")
    void testCustomOpenAPI_HasContactInfo() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Then
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getEmail()).isNotNull();
    }
    
    @Test
    @DisplayName("Should have license information")
    void testCustomOpenAPI_HasLicenseInfo() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Then
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("MIT License");
    }
    
    @Test
    @DisplayName("Should have JWT security scheme")
    void testCustomOpenAPI_HasJwtSecurityScheme() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Then
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        
        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(securityScheme.getType().toString()).isEqualToIgnoringCase("HTTP");
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }
    
    @Test
    @DisplayName("Should have servers configured")
    void testCustomOpenAPI_HasServers() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Then
        assertThat(openAPI.getServers()).isNotNull();
        assertThat(openAPI.getServers()).hasSize(2);
        assertThat(openAPI.getServers().get(0).getUrl()).contains("localhost:8080");
        assertThat(openAPI.getServers().get(0).getDescription()).contains("Local");
    }
    
    @Test
    @DisplayName("Should have security requirement")
    void testCustomOpenAPI_HasSecurityRequirement() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        
        // Then
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}

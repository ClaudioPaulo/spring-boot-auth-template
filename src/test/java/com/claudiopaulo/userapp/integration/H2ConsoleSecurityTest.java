package com.claudiopaulo.userapp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.h2.console.enabled=true")
@DisplayName("H2 Console Security Tests")
class H2ConsoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should permit the H2 console path and allow same-origin frames when the console is enabled")
    void h2ConsoleIsPermittedWhenEnabled() throws Exception {
        var response = mockMvc.perform(get("/h2-console/")).andReturn().getResponse();

        // Security lets the request through (it would be 403 if the path were protected)
        assertNotEquals(403, response.getStatus());
        assertEquals("SAMEORIGIN", response.getHeader("X-Frame-Options"));
    }
}

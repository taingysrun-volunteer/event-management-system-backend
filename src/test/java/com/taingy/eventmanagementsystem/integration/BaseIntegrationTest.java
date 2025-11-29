package com.taingy.eventmanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests that provides:
 * - H2 in-memory database (configured in application-test.properties)
 * - Full Spring Boot application context
 * - MockMvc for HTTP testing
 * - Shared test configuration
 *
 * NOTE: This uses H2 in-memory database for faster tests that don't require Docker.
 * For production-like testing with PostgreSQL, consider using Testcontainers
 * (requires Docker to be running).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void baseSetUp() {

    }
}

package com.enjot.quickcommerce.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared base for tests that need a real PostgreSQL instance.
 * <p>
 * Uses the singleton-container pattern: one container is started in a static
 * initializer and reused by every test class (Ryuk reaps it at JVM shutdown).
 * This avoids the per-class start/stop lifecycle clashing with Spring's
 * application-context cache.
 */
public abstract class PostgresContainerSupport {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}

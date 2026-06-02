package com.enjot.quickcommerce.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "change-me-in-production-this-secret-must-be-at-least-256-bits-long-0123456789";

    private final JwtService jwtService = new JwtService(SECRET, 60_000L);

    @Test
    void generatesAndExtractsSubject() {
        String token = jwtService.generateToken("alice@example.com", "USER");

        assertThat(jwtService.extractSubject(token)).contains("alice@example.com");
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.generateToken("alice@example.com", "USER");

        assertThat(jwtService.extractSubject(token + "x")).isEmpty();
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L);
        String token = shortLived.generateToken("alice@example.com", "USER");
        Thread.sleep(20);

        assertThat(shortLived.extractSubject(token)).isEmpty();
    }

    @Test
    void rejectsGarbage() {
        assertThat(jwtService.extractSubject("not-a-jwt")).isEqualTo(Optional.empty());
    }
}

package com.rodsussumu.riachuelo_backend.application.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService service;

    @BeforeEach
    void setup() throws Exception {
        service = new TokenService();
        Field f = TokenService.class.getDeclaredField("secretKey");
        f.setAccessible(true);
        f.set(service, "test-secret");
    }

    @Test
    @DisplayName("generateToken should create signed JWT with subject and issuer")
    void generateToken_createsSignedJwt() {
        String token = service.generateToken("alice");
        assertNotNull(token);
        Algorithm alg = Algorithm.HMAC256("test-secret");
        var decoded = JWT.require(alg).withIssuer("backend-app").build().verify(token);
        assertEquals("alice", decoded.getSubject());
        assertTrue(decoded.getExpiresAt().toInstant().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("validateToken should return subject for valid token")
    void validateToken_returnsSubject() {
        String token = service.generateToken("bob");
        String subject = service.validateToken(token);
        assertEquals("bob", subject);
    }

    @Test
    @DisplayName("validateToken should return empty string for token signed with different key")
    void validateToken_returnsEmpty_whenWrongSignature() {
        Algorithm other = Algorithm.HMAC256("other-secret");
        String token = JWT.create()
                .withIssuer("backend-app")
                .withSubject("mallory")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(6000)))
                .sign(other);
        String subject = service.validateToken(token);
        assertEquals("", subject);
    }

    @Test
    @DisplayName("validateToken should return empty string for malformed token")
    void validateToken_returnsEmpty_whenMalformed() {
        String subject = service.validateToken("not-a-jwt");
        assertEquals("", subject);
    }
}

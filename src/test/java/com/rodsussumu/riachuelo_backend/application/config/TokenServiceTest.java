package com.rodsussumu.riachuelo_backend.application.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.GenerateTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("generateToken lança GenerateTokenException quando sign dispara JWTCreationException")
    void generateToken_throwGenerateTokenException_onJwtCreationFailure() {
        TokenService service = new TokenService();
        ReflectionTestUtils.setField(service, "secretKey", "secret");

        JWTCreator.Builder builder = Mockito.mock(JWTCreator.Builder.class, Mockito.RETURNS_SELF);
        when(builder.sign(Mockito.any(Algorithm.class))).thenThrow(new JWTCreationException("fail", null));

        try (MockedStatic<JWT> jwtStatic = mockStatic(JWT.class)) {
            jwtStatic.when(JWT::create).thenReturn(builder);
            assertThrows(GenerateTokenException.class, () -> service.generateToken("alice"));
        }
    }
}

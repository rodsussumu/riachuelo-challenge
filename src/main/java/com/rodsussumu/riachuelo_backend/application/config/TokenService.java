package com.rodsussumu.riachuelo_backend.application.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rodsussumu.riachuelo_backend.application.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    @Value("${security.token}")
    private String secretKey;

    private static final int EXPIRES_IN = 6000;

    public String generateToken(String username) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            Instant now = Instant.now();

            return JWT.create()
                    .withIssuer("backend-app")
                    .withSubject(username)
                    .withExpiresAt(now.plusSeconds(EXPIRES_IN))
                    .sign(algorithm);


        } catch(JWTCreationException exception){
            throw new RuntimeException("Error Generating Token");
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.require(algorithm)
                    .withIssuer("backend-app")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch(JWTVerificationException exception) {
            return "";
        }
    }
}

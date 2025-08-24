package com.rodsussumu.riachuelo_backend.application.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieHelper {
    public static final String COOKIE_NAME = "ACCESS_TOKEN";

    public ResponseCookie buildAuthCookie(String token, Duration ttl, boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("None")
                .path("/")
                .maxAge(ttl)
                .build();
    }

    public ResponseCookie clearCookie(boolean secure) {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }
}

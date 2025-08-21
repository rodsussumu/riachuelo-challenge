package com.rodsussumu.riachuelo_backend.application.config;


import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Duration;

@RestControllerAdvice
public class AuthCookieAdvice implements ResponseBodyAdvice<UserAuthResponseDTO> {

    private static final String COOKIE_NAME = "ACCESS_TOKEN";
    private static final Duration TTL = Duration.ofSeconds(6000);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return UserAuthResponseDTO.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public UserAuthResponseDTO beforeBodyWrite(
            UserAuthResponseDTO body,
            MethodParameter returnType,
            org.springframework.http.MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            org.springframework.http.server.ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body != null && body.token() != null && response instanceof ServletServerHttpResponse r) {
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, body.token())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(TTL)
                    .build();
            r.getServletResponse().addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return body;
    }
}
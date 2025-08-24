package com.rodsussumu.riachuelo_backend.application.config;

import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Duration;

@RestControllerAdvice
public class AuthCookieAdvice implements ResponseBodyAdvice<Object> {

    private static final String COOKIE_NAME = "ACCESS_TOKEN";
    private static final Duration TTL = Duration.ofSeconds(6000);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> declared = returnType.getParameterType();
        if (UserAuthResponseDTO.class.isAssignableFrom(declared)) return true;

        ResolvableType rt = ResolvableType.forMethodParameter(returnType);
        if (rt.hasGenerics()) {
            Class<?> generic = rt.getGeneric(0).resolve();
            return generic != null && UserAuthResponseDTO.class.isAssignableFrom(generic);
        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        String token = TokenHolder.getToken();
        if (token != null) {
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                    .httpOnly(true)
                    .secure(true) // ⚠️ em produção
                    .sameSite("None")
                    .path("/")
                    .maxAge(TTL)
                    .build();
            response.getHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());

            TokenHolder.clear();
        }
        return body;
    }
}

package com.rodsussumu.riachuelo_backend.application.config;

import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthCookieAdviceTest {

    static class Dummy {
        public UserAuthResponseDTO ok() { return null; }
        public String other() { return null; }
    }

    private MethodParameter returnType(Method m) {
        return MethodParameter.forExecutable(m, -1);
    }

    @Test
    @DisplayName("supports returns true for UserAuthResponseDTO")
    void supports_true() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        MethodParameter mp = returnType(Dummy.class.getMethod("ok"));
        assertTrue(advice.supports(mp, MappingJackson2HttpMessageConverter.class));
    }

    @Test
    @DisplayName("supports returns false for other return types")
    void supports_false() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        MethodParameter mp = returnType(Dummy.class.getMethod("other"));
        assertFalse(advice.supports(mp, MappingJackson2HttpMessageConverter.class));
    }

    @Test
    @DisplayName("beforeBodyWrite sets cookie when body and token exist and response is ServletServerHttpResponse")
    void beforeBodyWrite_setsCookie_trueBranch() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletServerHttpRequest httpReq = new ServletServerHttpRequest(req);
        ServletServerHttpResponse httpRes = new ServletServerHttpResponse(res);

        UserAuthResponseDTO body = UserAuthResponseDTO.builder()
                .username("john")
                .token("jwt123")
                .build();

        advice.beforeBodyWrite(
                body,
                returnType(Dummy.class.getMethod("ok")),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        String setCookie = res.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("ACCESS_TOKEN=jwt123"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("SameSite=Strict"));
        assertTrue(setCookie.contains("Path=/"));
        assertTrue(setCookie.contains("Max-Age=6000"));
    }

    @Test
    @DisplayName("beforeBodyWrite does nothing when body is null")
    void beforeBodyWrite_noBody() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletServerHttpRequest httpReq = new ServletServerHttpRequest(req);
        ServletServerHttpResponse httpRes = new ServletServerHttpResponse(res);

        UserAuthResponseDTO out = advice.beforeBodyWrite(
                null,
                returnType(Dummy.class.getMethod("ok")),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        assertNull(out);
        assertNull(res.getHeader(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("beforeBodyWrite does nothing when token is null")
    void beforeBodyWrite_noToken() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletServerHttpRequest httpReq = new ServletServerHttpRequest(req);
        ServletServerHttpResponse httpRes = new ServletServerHttpResponse(res);

        UserAuthResponseDTO body = UserAuthResponseDTO.builder()
                .username("john")
                .token(null)
                .build();

        UserAuthResponseDTO out = advice.beforeBodyWrite(
                body,
                returnType(Dummy.class.getMethod("ok")),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        assertEquals(body, out);
        assertNull(res.getHeader(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("beforeBodyWrite with non-servlet response evaluates instanceof to false (no cookie)")
    void beforeBodyWrite_nonServletResponse_noCookie() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        ServerHttpRequest httpReq = mock(ServerHttpRequest.class);
        ServerHttpResponse httpRes = mock(ServerHttpResponse.class);

        UserAuthResponseDTO body = UserAuthResponseDTO.builder()
                .username("john")
                .token("jwt123")
                .build();

        UserAuthResponseDTO out = advice.beforeBodyWrite(
                body,
                returnType(Dummy.class.getMethod("ok")),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        assertEquals(body, out);
        verifyNoInteractions(httpRes);
    }
}

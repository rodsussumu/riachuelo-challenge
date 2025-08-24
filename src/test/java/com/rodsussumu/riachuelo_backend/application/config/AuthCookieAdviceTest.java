package com.rodsussumu.riachuelo_backend.application.config;

import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthCookieAdviceTest {

    // Métodos “dummy” apenas para obter MethodParameter com diferentes retornos
    static class Dummy {
        public UserAuthResponseDTO dto() { return null; }
        public ResponseEntity<UserAuthResponseDTO> entity() { return null; }
        public List<UserAuthResponseDTO> list() { return null; }
        public String str() { return null; }
        public List<String> listStr() { return null; }
    }

    private MethodParameter returnType(String methodName) throws NoSuchMethodException {
        Method m = Dummy.class.getMethod(methodName);
        return MethodParameter.forExecutable(m, -1);
    }

    @AfterEach
    void tearDown() {
        TokenHolder.clear();
    }

    // ---------- supports(..) ----------

    @Test
    @DisplayName("supports: true para retorno direto UserAuthResponseDTO")
    void supports_true_for_dto() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        assertTrue(advice.supports(
                returnType("dto"),
                MappingJackson2HttpMessageConverter.class));
    }

    @Test
    @DisplayName("supports: true para retorno genérico ResponseEntity<UserAuthResponseDTO>")
    void supports_true_for_generic_entity() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        assertTrue(advice.supports(
                returnType("entity"),
                MappingJackson2HttpMessageConverter.class));
    }

    @Test
    @DisplayName("supports: true para retorno List<UserAuthResponseDTO>")
    void supports_true_for_list_of_dto() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        assertTrue(advice.supports(
                returnType("list"),
                MappingJackson2HttpMessageConverter.class));
    }

    @Test
    @DisplayName("supports: false para tipos que não contém UserAuthResponseDTO")
    void supports_false_for_other_types() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();
        assertFalse(advice.supports(
                returnType("str"),
                MappingJackson2HttpMessageConverter.class));
        assertFalse(advice.supports(
                returnType("listStr"),
                MappingJackson2HttpMessageConverter.class));
    }

    // ---------- beforeBodyWrite(..) ----------

    @Test
    @DisplayName("beforeBodyWrite: seta cookie e limpa TokenHolder quando há token")
    void beforeBodyWrite_sets_cookie_and_clears_token() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletServerHttpRequest httpReq = new ServletServerHttpRequest(req);
        ServletServerHttpResponse httpRes = new ServletServerHttpResponse(res);

        TokenHolder.setToken("jwt123");

        UserAuthResponseDTO body = UserAuthResponseDTO.builder()
                .username("john")
                .authenticated(true)
                .build();

        Object out = advice.beforeBodyWrite(
                body,
                returnType("dto"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        httpRes.flush();

        String setCookie = res.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("ACCESS_TOKEN=jwt123"));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("SameSite=None"));
        assertTrue(setCookie.contains("Path=/"));
        assertTrue(setCookie.contains("Max-Age=6000"));

        assertEquals(body, out);
        assertNull(TokenHolder.getToken(), "TokenHolder deve ser limpo após escrever o cookie");
    }

    @Test
    @DisplayName("beforeBodyWrite: não seta cookie quando não há token")
    void beforeBodyWrite_no_token_no_cookie() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletServerHttpRequest httpReq = new ServletServerHttpRequest(req);
        ServletServerHttpResponse httpRes = new ServletServerHttpResponse(res);

        TokenHolder.clear();

        UserAuthResponseDTO body = UserAuthResponseDTO.builder()
                .username("john")
                .authenticated(true)
                .build();

        Object out = advice.beforeBodyWrite(
                body,
                returnType("dto"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        httpRes.flush();

        assertNull(res.getHeader(HttpHeaders.SET_COOKIE));
        assertEquals(body, out);
    }

    @Test
    @DisplayName("beforeBodyWrite: body nulo ainda deve setar cookie se houver token")
    void beforeBodyWrite_null_body_still_sets_cookie_if_token() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        ServletServerHttpRequest httpReq = new ServletServerHttpRequest(req);
        ServletServerHttpResponse httpRes = new ServletServerHttpResponse(res);

        TokenHolder.setToken("jwt123");

        Object out = advice.beforeBodyWrite(
                null,
                returnType("dto"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        httpRes.flush();

        String setCookie = res.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("ACCESS_TOKEN=jwt123"));
        assertNull(out);
        assertNull(TokenHolder.getToken());
    }

    @Test
    @DisplayName("beforeBodyWrite: funciona com ServerHttpResponse não-servlet")
    void beforeBodyWrite_non_servlet_response() throws Exception {
        AuthCookieAdvice advice = new AuthCookieAdvice();

        ServerHttpRequest httpReq = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        ServerHttpResponse httpRes = mock(ServerHttpResponse.class);
        when(httpRes.getHeaders()).thenReturn(headers);

        TokenHolder.setToken("jwt123");

        UserAuthResponseDTO body = UserAuthResponseDTO.builder()
                .username("john")
                .authenticated(true)
                .build();

        Object out = advice.beforeBodyWrite(
                body,
                returnType("dto"),
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                httpReq,
                httpRes
        );

        assertEquals(body, out);
        String setCookie = headers.getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("ACCESS_TOKEN=jwt123"));
    }
}

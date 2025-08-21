package com.rodsussumu.riachuelo_backend.application.config;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

  @Mock
  TokenService tokenService;

  @Mock
  org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  @Mock
  FilterChain filterChain;

  SecurityFilter filter;

  @BeforeEach
  void setup() {
    filter = new SecurityFilter(tokenService, userDetailsService);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("should bypass /auth/login without authentication")
  void bypass_login() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/auth/login");
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("should bypass /auth/register without authentication")
  void bypass_register() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/auth/register");
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("should not authenticate when Authorization header is missing")
  void no_header() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/tasks");
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verifyNoInteractions(tokenService, userDetailsService);
  }

  @Test
  @DisplayName("should not authenticate when Authorization header is blank")
  void blank_header() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/tasks");
    req.addHeader("Authorization", "  ");
    MockHttpServletResponse res = new MockHttpServletResponse();

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verifyNoInteractions(tokenService, userDetailsService);
  }

  @Test
  @DisplayName("should set authentication when token is valid with Bearer prefix")
  void valid_token_with_bearer() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/tasks");
    req.addHeader("Authorization", "Bearer abc");
    MockHttpServletResponse res = new MockHttpServletResponse();

    when(tokenService.validateToken("abc")).thenReturn("john");
    UserDetails user = User.withUsername("john").password("x").build();
    when(userDetailsService.loadUserByUsername("john")).thenReturn(user);

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals("john", ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
  }

  @Test
  @DisplayName("should set authentication when token is valid without Bearer prefix")
  void valid_token_without_bearer() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/tasks");
    req.addHeader("Authorization", "abc");
    MockHttpServletResponse res = new MockHttpServletResponse();

    when(tokenService.validateToken("abc")).thenReturn("john");
    UserDetails user = User.withUsername("john").password("x").build();
    when(userDetailsService.loadUserByUsername("john")).thenReturn(user);

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals("john", ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
  }

  @Test
  @DisplayName("should not authenticate when token validation returns null")
  void invalid_token_returns_null_username() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/tasks");
    req.addHeader("Authorization", "Bearer bad");
    MockHttpServletResponse res = new MockHttpServletResponse();

    when(tokenService.validateToken("bad")).thenReturn(null);

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(userDetailsService, never()).loadUserByUsername(anyString());
  }

  @Test
  @DisplayName("should not authenticate when userDetailsService returns null")
  void valid_token_but_user_not_found() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServletPath("/tasks");
    req.addHeader("Authorization", "Bearer abc");
    MockHttpServletResponse res = new MockHttpServletResponse();

    when(tokenService.validateToken("abc")).thenReturn("john");
    when(userDetailsService.loadUserByUsername("john")).thenReturn(null);

    filter.doFilter(req, res, filterChain);

    verify(filterChain, times(1)).doFilter(ArgumentMatchers.any(), ArgumentMatchers.any());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }
}

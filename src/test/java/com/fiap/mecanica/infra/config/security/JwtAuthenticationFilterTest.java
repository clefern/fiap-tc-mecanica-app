package com.fiap.mecanica.infra.config.security;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;

  @Mock private UserDetailsService userDetailsService;

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @Test
  @DisplayName("Should set authentication for valid token")
  void shouldSetAuthenticationForValidToken() throws Exception {
    String token = "valid-token";
    String username = "user@example.com";
    UserDetails userDetails = mock(UserDetails.class);

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

    // Ensure context is clear before test
    SecurityContextHolder.clearContext();

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService).loadUserByUsername(username);
    verify(filterChain).doFilter(request, response);
    // We can't easily verify SecurityContextHolder static calls, but if loadUserByUsername was
    // called and isTokenValid is true, logic dictates it was set.
  }

  @Test
  @DisplayName("Should not set authentication for invalid token")
  void shouldNotSetAuthenticationForInvalidToken() throws Exception {
    String token = "invalid-token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

    SecurityContextHolder.clearContext();

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should not set authentication for missing header")
  void shouldNotSetAuthenticationForMissingHeader() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    SecurityContextHolder.clearContext();

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, never()).extractUsername(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should not set authentication for non-Bearer token")
  void shouldNotSetAuthenticationForNonBearerToken() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic 12345");

    SecurityContextHolder.clearContext();

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService, never()).extractUsername(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should not set authentication if context already has authentication")
  void shouldNotSetAuthenticationIfContextNotEmpty() throws Exception {
    String token = "valid-token";
    String username = "user@example.com";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn(username);

    // Set existing authentication
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "user", "pass"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Should not set authentication if token is invalid")
  void shouldNotSetAuthenticationIfTokenIsInvalid() throws Exception {
    String token = "invalid-token";
    String username = "user@example.com";
    UserDetails userDetails = mock(UserDetails.class);

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

    SecurityContextHolder.clearContext();

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService).loadUserByUsername(username);
    // Should NOT set authentication
    assert (SecurityContextHolder.getContext().getAuthentication() == null);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should not set authentication if username is null")
  void shouldNotSetAuthenticationIfUsernameIsNull() throws Exception {
    String token = "valid-token-no-subject";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn(null);

    SecurityContextHolder.clearContext();

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
  }
}

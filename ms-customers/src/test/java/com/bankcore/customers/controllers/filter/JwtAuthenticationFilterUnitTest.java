package com.bankcore.customers.controllers.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bankcore.customers.exceptions.NoAuthoritiesException;
import com.bankcore.customers.services.JwtService;
import com.bankcore.customers.utils.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterUnitTest {

  @Mock private JwtService jwtService;

  @Mock private FilterChain filterChain;

  @Mock private HandlerExceptionResolver exceptionResolver;

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // No token or invalid token:

  @Test
  void shouldNotAuthenticate_whenNoAuthorizationHeader() throws ServletException, IOException {

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldNotAuthenticate_whenAuthorizationHeaderIsEmpty() throws ServletException, IOException {
    request.addHeader("Authorization", "");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldNotAuthenticate_whenHeaderIsNoBearer() throws ServletException, IOException {
    request.addHeader("Authorization", "No Bearer token");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldNotAuthenticate_whenTokenIsInvalid() throws ServletException, IOException {
    String invalidToken = "invalid.jwt.token";
    request.addHeader("Authorization", "Bearer " + invalidToken);

    when(jwtService.validateToken(invalidToken)).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(jwtService).validateToken(invalidToken);
    verify(jwtService, never()).getUUIDfromToken(invalidToken);
    verify(jwtService, never()).getRolesFromToken(invalidToken);
    verify(filterChain).doFilter(request, response);
  }

  // Valid token:

  @Test
  void shouldAuthenticate_whenTokenIsValid() throws ServletException, IOException {

    String validToken = "valid.jwt.token";
    String uuid = UUID.randomUUID().toString();
    List<String> roles = List.of("ROLE_" + UserRole.CUSTOMER.name());

    request.addHeader("Authorization", "Bearer " + validToken);

    when(jwtService.validateToken(validToken)).thenReturn(true);
    when(jwtService.getUUIDfromToken(validToken)).thenReturn(uuid);
    when(jwtService.getRolesFromToken(validToken)).thenReturn(roles);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isEqualTo(uuid);
    assertThat(authentication.getCredentials()).isNull();
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_" + UserRole.CUSTOMER.name());

    verify(filterChain).doFilter(request, response);
  }

  // Exception Managment:

  @Test
  void shouldNotContinueFilterChain_whenJwtServiceThrowsException() throws Exception {
    request.addHeader("Authorization", "Bearer some-token");

    when(jwtService.validateToken(anyString()))
        .thenThrow(new NoAuthoritiesException("no authorities"));
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);

    verify(exceptionResolver)
        .resolveException(eq(request), eq(response), isNull(), any(NoAuthoritiesException.class));
  }

  @Test
  void shouldNotContinueFilterChain_whenUUIDThrowsException() throws ServletException, IOException {
    String token = "Some token";
    request.addHeader("Authorization", "Bearer " + token);

    when(jwtService.validateToken(token)).thenReturn(true);
    when(jwtService.getUUIDfromToken(token))
        .thenThrow(new RuntimeException("UUID extraction error"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
    verify(exceptionResolver)
        .resolveException(eq(request), eq(response), isNull(), any(RuntimeException.class));
  }

  // JWT parse:

  @Test
  void shouldCorrectlyParseToken_fromBearerHeader() throws ServletException, IOException {
    String baseToken = "eyJhbGciOiJIUzI1NiJ9.payload.signature";
    request.addHeader("Authorization", "Bearer " + baseToken);

    when(jwtService.validateToken(baseToken)).thenReturn(true);
    when(jwtService.getUUIDfromToken(baseToken)).thenReturn(UUID.randomUUID().toString());
    when(jwtService.getRolesFromToken(baseToken)).thenReturn(List.of("ROLENUM1"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(jwtService).validateToken(baseToken);
    verify(jwtService).getUUIDfromToken(baseToken);
  }

  @Test
  void shouldNotAuthenticate_whenHeaderIsBearerWithNoToken() throws ServletException, IOException {

    request.addHeader("Authorization", "Bearer ");

    when(jwtService.validateToken("")).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }
}

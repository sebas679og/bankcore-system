package com.bankcore.customers.controllers.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bankcore.customers.AbstractIntegrationTest;
import com.bankcore.customers.services.JwtService;
import com.bankcore.customers.utils.enums.UserRole;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

public class JwtAuthenticationFilterIntegrationTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtService jwtService;

  @Autowired private SecretKey secretKey;

  // Helper:
  private String generateToken(String uuid, String... role) {
    List<SimpleGrantedAuthority> authorities =
        Stream.of(role).map(SimpleGrantedAuthority::new).toList();

    UserDetails userDetails =
        User.builder().username(uuid).password("irrelevant").authorities(authorities).build();

    return jwtService.generateAccessToken(userDetails);
  }

  private String generateTokenWithNoRolesAndNoRolesClaim(String subject) {
    return Jwts.builder()
        .subject(subject)
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 200000))
        .claim("enabled", true)
        .issuer("bankcore")
        .signWith(secretKey)
        .compact();
  }

  private String generateTokenWithNoRolesWithRolesClaim(String subject) {
    return Jwts.builder()
        .subject(subject)
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .claim("roles", List.of())
        .claim("enabled", true)
        .issuer("bankcore")
        .signWith(secretKey)
        .compact();
  }

  // No token:
  @Test
  void shouldReturn401_whenNoAuthorizationHeader() throws Exception {
    mockMvc.perform(get("/api/customers/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenBearerTokenIsEmpty() throws Exception {

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer "))
        .andExpect(status().isUnauthorized());
  }

  // Invalid Token:

  @Test
  void shouldReturn401_whenTokenIsMalformed() throws Exception {
    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer invalid.jwt.token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenSignatureIsTampered() throws Exception {
    String validToken =
        generateToken(UUID.randomUUID().toString(), "ROLE_" + UserRole.CUSTOMER.name());
    String[] parts = validToken.split("\\.");
    String tamperedToken = parts[0] + "." + parts[1] + ".fake-manipulated-signature";

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + tamperedToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenPayLoadIsTampered() throws Exception {
    String validToken =
        generateToken(UUID.randomUUID().toString(), "ROLE_" + UserRole.CUSTOMER.name());
    String[] parts = validToken.split("\\.");
    String tamperedToken =
        parts[0]
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0."
            + parts[2];

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + tamperedToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenTokenIsExpired() throws Exception {
    String expiredExToken =
        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLXV1aWQiLCJleHAiOjE1Nzc4MzY4MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + expiredExToken))
        .andExpect(status().isUnauthorized());
  }

  // Valid Token But Incorrect Role:

  @Test
  void shouldReturn403_whenTokenHasWrongRole() throws Exception {
    String token = generateToken(UUID.randomUUID().toString(), "ROLE_USER");

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn403WhenTokenHasNoRolesAndNoRolesClaim() throws Exception {
    String token = generateTokenWithNoRolesAndNoRolesClaim(UUID.randomUUID().toString());

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + token))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  void shouldReturn403_whenTokenHasNoRolesWithRolesClaim() throws Exception {
    String token = generateTokenWithNoRolesWithRolesClaim(UUID.randomUUID().toString());

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + token))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  // Valid token

  @Test
  void shouldPassFilter_whenValidTokenWithCustomerRole() throws Exception {
    String token = generateToken(UUID.randomUUID().toString(), "ROLE_" + UserRole.CUSTOMER.name());

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + token))
        .andDo(print())
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
  }

  @Test
  void shouldPassFilter_whenValidTokenWithAdminRole() throws Exception {
    String token = generateToken(UUID.randomUUID().toString(), "ROLE_" + UserRole.ADMIN.name());

    mockMvc
        .perform(get("/api/customers/me").header("Authorization", "Bearer " + token))
        .andDo(print())
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
  }

  // Public Endpoints - no token required

  @Test
  void shouldAllow_publicAuthEndpoint_withoutToken() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "email": "test@bankcore.com",
                                  "password": "wrongpassword"
                                }
                                """))
        .andDo(print())
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
  }

  @Test
  void shouldAllow_swaggerUi_withoutToken() throws Exception {
    mockMvc
        .perform(get("/swagger-ui/index.html"))
        .andDo(print())
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
  }

  @Test
  void shouldDeny_unknownRoute_evenWithValidToken() throws Exception {
    String token = generateToken(UUID.randomUUID().toString(), "ROLE_" + UserRole.CUSTOMER.name());

    // SecurityConfig has .anyRequest().denyAll()
    mockMvc
        .perform(get("/api/ruta-inexistente").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }
}

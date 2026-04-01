package com.bankcore.customers.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bankcore.customers.exceptions.NoAuthoritiesException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtServiceUnitTest {

  private JwtService jwtService;
  private SecretKey secretKey;
  private UserDetails testUser;

  @BeforeEach
  void setUp() {
    String keyString = "my-super-secret-key-for-bankcore-tests";
    secretKey = Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));

    jwtService = new JwtService(secretKey);

    testUser =
        User.builder()
            .username("550e8400-e29b-41d4-a716-446655440000")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
            .build();
  }

  @Test
  void shouldReturnNonBlankToken_whenUserIsValid() {
    String token = jwtService.generateAccessToken(testUser);

    assertThat(token).isNotBlank();
    assertThat(token.split("\\.")).hasSize(3);
  }

  @Test
  void shouldContainCorrectSubject_whenTokenIsGenerated() {
    String token = jwtService.generateAccessToken(testUser);

    String subject = jwtService.getUuidFromToken(token);

    assertThat(subject).isEqualTo(testUser.getUsername());
  }

  @Test
  void shouldContainCorrectRoles_whenTokenIsGenerated() {
    String token = jwtService.generateAccessToken(testUser);

    List<String> roles = jwtService.getRolesFromToken(token);

    assertThat(roles).hasSize(1).containsExactlyInAnyOrder("ROLE_CUSTOMER");
  }

  @Test
  void shouldHaveCorrectExpiration_whenTokenIsGenerated() {
    long beforeGenerationSeconds = System.currentTimeMillis() / 1000;
    String token = jwtService.generateAccessToken(testUser);
    long afterGenerationSeconds = System.currentTimeMillis() / 1000;

    long expirationSeconds = jwtService.getAccessTokenExpiration(token);
    long expirationWindowSeconds = JwtService.ACCESS_TOKEN_EXPIRATION / 1000;

    assertThat(expirationSeconds)
        .isGreaterThanOrEqualTo(beforeGenerationSeconds + expirationWindowSeconds)
        .isLessThanOrEqualTo(afterGenerationSeconds + expirationWindowSeconds + 1);
  }

  @Test
  void shouldGenerateUniqueTokenId_onEachCall() {
    String token1 = jwtService.generateAccessToken(testUser);
    String token2 = jwtService.generateAccessToken(testUser);

    assertThat(token1).isNotEqualTo(token2);
  }

  @Test
  void shouldNotGenerateToken_whenUserHasNoRoles() {
    UserDetails userWithNoRoles =
        User.builder()
            .username("uuid-sin-roles")
            .password("password")
            .authorities(List.of())
            .build();

    assertThrows(
        NoAuthoritiesException.class,
        () -> {
          jwtService.generateAccessToken(userWithNoRoles);
        });
  }

  @Nested
  class ValidateToken {

    @Test
    void shouldReturnTrue_whenTokenIsValid() {
      String token = jwtService.generateAccessToken(testUser);

      boolean isValid = jwtService.validateToken(token);

      assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalse_whenTokenIsExpired() {
      String expiredToken =
          Jwts.builder()
              .subject(testUser.getUsername())
              .issuedAt(new Date(System.currentTimeMillis() - 30 * 60 * 1000))
              .expiration(new Date(System.currentTimeMillis() - 15 * 60 * 1000))
              .signWith(secretKey)
              .compact();

      boolean isValid = jwtService.validateToken(expiredToken);

      assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalse_whenTokenIsMalformed() {
      boolean isValid = jwtService.validateToken("esto.no.es.un.jwt.valido");

      assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalse_whenTokenHasInvalidSignature() {
      SecretKey differentKey =
          Keys.hmacShaKeyFor(
              "otra-clave-completamente-diferente-para-test".getBytes(StandardCharsets.UTF_8));

      String tokenWithWrongSignature =
          Jwts.builder()
              .subject(testUser.getUsername())
              .issuedAt(new Date())
              .expiration(new Date(System.currentTimeMillis() + 60000))
              .signWith(differentKey)
              .compact();

      boolean isValid = jwtService.validateToken(tokenWithWrongSignature);

      assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalse_whenTokenIsNullOrEmpty() {
      assertThat(jwtService.validateToken(null)).isFalse();
      assertThat(jwtService.validateToken("")).isFalse();
      assertThat(jwtService.validateToken("   ")).isFalse();
    }
  }

  @Nested
  class GetUUIDfromToken {

    @Test
    void shouldReturnCorrectUUID() {
      String token = jwtService.generateAccessToken(testUser);

      String uuid = jwtService.getUuidFromToken(token);

      assertThat(uuid).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }
  }

  @Nested
  class GetRolesFromToken {

    @Test
    void shouldReturnCorrectRoles() {
      String token = jwtService.generateAccessToken(testUser);

      List<String> roles = jwtService.getRolesFromToken(token);

      assertThat(roles).containsExactlyInAnyOrder("ROLE_CUSTOMER");
    }

    @Test
    void shouldReturnNull_whenNoRolesClaim() {

      UserDetails noRolesUser =
          User.builder().username("uuid-vacio").password("pass").authorities(List.of()).build();

      String token =
          Jwts.builder()
              .subject(noRolesUser.getUsername())
              .id(UUID.randomUUID().toString())
              .issuedAt(new Date())
              .claim("enabled", true)
              .issuer("bankcore")
              .signWith(secretKey)
              .compact();

      List<String> roles = jwtService.getRolesFromToken(token);

      assertThat(roles).isNull();
    }

    @Test
    void shouldReturnEmpty_whenNoRolesInRolesClaim() {

      UserDetails noRolesUser =
          User.builder().username("uuid-vacio").password("pass").authorities(List.of()).build();

      String token =
          Jwts.builder()
              .subject(noRolesUser.getUsername())
              .id(UUID.randomUUID().toString())
              .issuedAt(new Date())
              .claim("roles", noRolesUser.getAuthorities())
              .claim("enabled", true)
              .issuer("bankcore")
              .signWith(secretKey)
              .compact();

      List<String> roles = jwtService.getRolesFromToken(token);

      assertThat(roles).isEmpty();
    }
  }
}

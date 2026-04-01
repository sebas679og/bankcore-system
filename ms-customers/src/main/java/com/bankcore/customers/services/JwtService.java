package com.bankcore.customers.services;

import com.bankcore.customers.exceptions.NoAuthoritiesException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing JSON Web Tokens (JWT). Provides utility methods for generating,
 * parsing, and validating access tokens used for authentication and authorization within the
 * bankcore system.
 *
 * @author BankCore Team
 * @author Cristian Ortiz
 * @version 0.1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

  private final SecretKey secretKey;

  public static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;

  /**
   * Generates a signed access token for a specific user. Includes standard claims (subject, id,
   * issuedAt, expiration, issuer) and custom claims (roles, enabled).
   *
   * @param userDetails The user details to be encoded in the token.
   * @return A compact, URL-safe JWT string.
   */
  public String generateAccessToken(UserDetails userDetails) {
    List<String> authorities =
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

    if (authorities.isEmpty()) {
      throw new NoAuthoritiesException("User has no granted authorities");
    }

    return Jwts.builder()
        .subject(userDetails.getUsername())
        .id(UUID.randomUUID().toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .claim("roles", authorities)
        .claim("enabled", userDetails.isEnabled())
        .issuer("bankcore")
        .signWith(secretKey)
        .compact();
  }

  /**
   * Extracts the uuid (subject) from a given JWT.
   *
   * @param token The JWT string.
   * @return The uuid contained in the token payload.
   * @throws JwtException if the token is invalid or cannot be parsed.
   */
  public String getUuidFromToken(String token) {
    return getClaims(token).getSubject();
  }

  /**
   * Retrieves the set of roles/authorities assigned to the user from the token.
   *
   * @param token The JWT string.
   * @return A Set of role names.
   */
  @SuppressWarnings("unchecked")
  public List<String> getRolesFromToken(String token) {
    return getClaims(token).get("roles", ArrayList.class);
  }

  public long getAccessTokenExpiration(String token) {
    return getClaims(token).getExpiration().getTime() / 1000;
  }

  /**
   * Validates the integrity, signature, and expiration of a JWT. This method logs specific security
   * errors if the validation fails.
   *
   * @param token The JWT string to validate.
   * @return {@code true} if the token is valid, {@code false} otherwise.
   */
  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;

    } catch (ExpiredJwtException e) {
      log.error("Expired token: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Malformed token: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("Token is empty or null: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Token problem: {}", e.getMessage());
    }

    return false;
  }

  /**
   * Parses the JWT and returns its claims payload.
   *
   * @param token The JWT string.
   * @return The {@link Claims} object.
   * @throws JwtException if the token cannot be verified or parsed.
   */
  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}

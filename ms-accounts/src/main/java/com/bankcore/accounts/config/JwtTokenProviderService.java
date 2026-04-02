package com.bankcore.accounts.config;

import com.bankcore.accounts.utils.enums.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Service responsible for generating JWT tokens used for internal service-to-service communication
 * within the Bankcore system.
 *
 * <p>This implementation creates a short-lived token with the SERVICE role, intended to be used
 * exclusively by trusted internal microservices (e.g., ms-accounts) when invoking protected
 * endpoints.
 *
 * <p>The generated token includes:
 *
 * <ul>
 *   <li>Issuer: bankcore
 *   <li>Subject: BANKCORE_SYSTEM_ACCOUNTS
 *   <li>Role: SERVICE
 *   <li>Expiration time: 3 minutes
 * </ul>
 *
 * @author Bankcore Team - Sebastian Orjuela
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProviderService {

  private final JwtEncoder jwtEncoder;

  /**
   * Generates a signed JWT token for internal system usage.
   *
   * <p>This token is intended for service-level authentication and should not be used for end-user
   * authentication flows.
   *
   * @return a signed JWT token containing SERVICE role authority
   */
  public String generateServiceToken() {

    Instant now = Instant.now();

    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("bankcore")
            .issuedAt(now)
            .expiresAt(now.plus(3, ChronoUnit.MINUTES))
            .subject("BANKCORE_SYSTEM_ACCOUNTS")
            .claim("roles", List.of(String.join("", "ROLE_", UserRole.SERVICE.name())))
            .build();

    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }
}

package com.bankcore.accounts.config;

import com.bankcore.accounts.exceptions.CustomAccessDeniedHandler;
import com.bankcore.accounts.exceptions.CustomAuthenticationEntryPoint;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;

/**
 * Security configuration for the Accounts microservice.
 * <p>
 * Configures JWT-based authentication and authorization using Spring Security's
 * OAuth2 Resource Server support. Defines the security filter chain, JWT decoding,
 * role extraction from token claims, and custom exception handling for
 * authentication and access denied scenarios.
 * </p>
 *
 * <p><b>Access Rules:</b></p>
 * <ul>
 *   <li>Swagger UI and OpenAPI docs are publicly accessible.</li>
 *   <li>{@code /api/accounts/**} requires the {@code CUSTOMER} role.</li>
 *   <li>All other requests are denied by default.</li>
 * </ul>
 *
 * @author BankCore Team - Sebastian Orjuela - Cristian Ortiz
 * @version 1.0
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    /**
     * Creates the {@link SecretKey} used to verify JWT signatures.
     * <p>
     * The secret is decoded from a Base64URL-encoded string defined in the
     * application properties under {@code spring.security.oauth2.resourceserver.jwt.secret-key}.
     * </p>
     *
     * @param secret the Base64URL-encoded HMAC secret key
     * @return a {@link SecretKey} suitable for HS256 JWT verification
     */
    @Bean
    public SecretKey jwtSecretKey(@Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {
        String cleanSecret = secret.trim();
        byte[] keyBytes = Decoders.BASE64URL.decode(cleanSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Creates the {@link JwtDecoder} used to parse and validate incoming JWT tokens.
     * <p>
     * Uses {@link NimbusJwtDecoder} with HMAC-SHA256 ({@link MacAlgorithm#HS256})
     * and the provided {@link SecretKey}.
     * </p>
     *
     * @param secretKey the secret key used for JWT signature verification
     * @return a configured {@link JwtDecoder}
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Creates a {@link JwtAuthenticationConverter} that extracts roles from the JWT claims.
     * <p>
     * Reads granted authorities from the {@code roles} claim and prefixes each value
     * with {@code ROLE_} to align with Spring Security's role-based authorization conventions.
     * </p>
     *
     * @return a configured {@link JwtAuthenticationConverter}
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("");
        converter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }

    /**
     * Configures the {@link SecurityFilterChain} for the Accounts microservice.
     * <p>
     * Applies the following security rules:
     * </p>
     * <ul>
     *   <li>Custom {@link CustomAuthenticationEntryPoint} handles 401 Unauthorized responses.</li>
     *   <li>Custom {@link CustomAccessDeniedHandler} handles 403 Forbidden responses.</li>
     *   <li>CSRF protection is disabled (stateless JWT-based API).</li>
     *   <li>Swagger UI and OpenAPI docs ({@code /swagger-ui/**}, {@code /v3/api-docs/**}) are publicly accessible.</li>
     *   <li>{@code /api/accounts/**} requires the {@code CUSTOMER} role.</li>
     *   <li>All other requests are denied.</li>
     * </ul>
     *
     * @param http                         the {@link HttpSecurity} builder
     * @param customAuthenticationEntryPoint custom handler for authentication failures
     * @param customAccessDeniedHandle       custom handler for authorization failures
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during security configuration
     */
    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandle
    ) throws Exception {
        http
                .exceptionHandling(
                        e -> e.authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandle))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                .requestMatchers("/api/accounts/**").hasRole("CUSTOMER")
                                .anyRequest()
                                .denyAll())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        ));

        return http.build();
    }

}
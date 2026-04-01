package com.bankcore.accounts.config;

import com.bankcore.accounts.exceptions.CustomAccessDeniedHandler;
import com.bankcore.accounts.exceptions.CustomAuthenticationEntryPoint;
import com.bankcore.accounts.utils.enums.UserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration class for the Accounts microservice. This class sets up JWT authentication
 * and authorization, defining how incoming requests are secured. It includes custom handlers for
 * authentication and access denied exceptions, as well as a converter for extracting roles from JWT
 * tokens.
 *
 * @author BankCore Team - Sebastian Orjuela
 * @version 0.1.0
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(
      HttpSecurity http,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
      CustomAccessDeniedHandler customAccessDeniedHandler,
      CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    http.exceptionHandling(
            e ->
                e.authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .headers(
            headers ->
                headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .cacheControl(Customizer.withDefaults()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts")
                    .hasRole(UserRole.CUSTOMER.name())
                    .requestMatchers(HttpMethod.GET, "/api/accounts")
                    .hasRole(UserRole.CUSTOMER.name())
                    .requestMatchers(HttpMethod.POST, "/api/transfers")
                    .hasRole(UserRole.CUSTOMER.name())
                    .requestMatchers(HttpMethod.GET, "/api/accounts/*")
                    .hasRole(UserRole.CUSTOMER.name())
                    .requestMatchers(HttpMethod.GET, "/api/accounts/*/transactions")
                    .hasRole(UserRole.CUSTOMER.name())
                    .requestMatchers(HttpMethod.POST, "/api/accounts/*/deposit")
                    .hasRole(UserRole.CUSTOMER.name())
                    .requestMatchers(HttpMethod.POST, "/api/accounts/*/withdraw")
                    .hasRole(UserRole.CUSTOMER.name())
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(
            oauth ->
                oauth
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .jwt(
                        jwt ->
                            jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter(jwtGrantedAuthoritiesConverter()))));

    return http.build();
  }

  /**
   * Configures the Cross-Origin Resource Sharing (CORS) settings for the application.
   *
   * <p>This bean defines a global CORS policy that allows controlled access from external origins,
   * configuring allowed methods, headers, and the preflight cache duration.
   *
   * @param allowedOrigins List of permitted origin URLs.
   * @param allowedMethods List of permitted HTTP methods (e.g., GET, POST).
   * @param maxAge Maximum time (in seconds) the browser should cache the preflight response.
   * @return A {@link CorsConfigurationSource} applied to all application paths.
   * @see org.springframework.web.cors.CorsConfiguration
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins}") List<String> allowedOrigins,
      @Value("${app.cors.allowed-methods}") List<String> allowedMethods,
      @Value("${app.cors.max-age}") long maxAge) {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(allowedMethods);
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setMaxAge(maxAge);
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * Configures the converter to extract authorities from JWT tokens. This converter looks for a
   * claim named "roles" and uses its values as authorities without any prefix.
   *
   * @return A configured {@link JwtGrantedAuthoritiesConverter} instance.
   */
  @Bean
  public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
    converter.setAuthoritiesClaimName("roles");
    converter.setAuthorityPrefix("");
    return converter;
  }

  /**
   * Configures the JWT authentication converter to use the custom granted authorities converter.
   *
   * @param grantedAuthoritiesConverter The converter that extracts authorities from JWT claims.
   * @return A configured {@link JwtAuthenticationConverter} instance.
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter(
      JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter) {

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return converter;
  }

  /**
   * Configures the secret key for JWT encoding and decoding. The secret key is expected to be
   * provided as a Base64-encoded string in the application properties.
   *
   * @param secret The Base64-encoded secret key string from application properties.
   * @return A {@link SecretKey} instance derived from the provided secret string.
   */
  @Bean
  public SecretKey secretKey(
      @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {

    byte[] decodedKey = Base64.getUrlDecoder().decode(secret.trim());

    return new SecretKeySpec(decodedKey, MacAlgorithm.HS256.getName());
  }

  /**
   * Configures the JWT decoder to use the provided secret key and the HS256 algorithm.
   *
   * @param secretKey The secret key used for decoding JWT tokens.
   * @return A {@link JwtDecoder} instance configured with the secret key and algorithm.
   */
  @Bean
  public JwtDecoder jwtDecoder(SecretKey secretKey) {
    return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
  }

  /**
   * Configures the JWT encoder to use the provided secret key. This encoder will be used to
   * generate JWT tokens for authentication.
   *
   * @param secretKey The secret key used for encoding JWT tokens.
   * @return A {@link JwtEncoder} instance configured with the secret key.
   */
  @Bean
  public JwtEncoder jwtEncoder(SecretKey secretKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
  }
}

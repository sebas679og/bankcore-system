package com.bankcore.customers.config;

import com.bankcore.customers.controllers.filter.JwtAuthenticationFilter;
import com.bankcore.customers.exceptions.CustomAccessDeniedHandler;
import com.bankcore.customers.exceptions.CustomAuthenticationEntryPoint;
import com.bankcore.customers.utils.enums.UserRole;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration class responsible for defining authentication and authorization rules for
 * the application.
 *
 * <p>This configuration:
 *
 * <ul>
 *   <li>Disables CSRF protection (commonly used in stateless REST APIs).
 *   <li>Allows unauthenticated access to authentication-related endpoints and documentation.
 *   <li>Enforces stateless session management using JWT.
 *   <li>Registers custom entry points for handling unauthorized and forbidden access.
 *   <li>Configures the {@link JwtAuthenticationFilter} within the security filter chain.
 * </ul>
 *
 * @author BankCore Team
 * @author Sebastian Orjuela
 * @author Cristian Ortiz
 * @version 0.1.0
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;

  /**
   * Configures the {@link SecurityFilterChain} to define the security behavior of the application.
   *
   * <p>It integrates custom handlers for authentication and authorization failures, ensures the
   * application is stateless, and secures endpoints based on user roles.
   *
   * @param http The {@link HttpSecurity} to modify.
   * @param customAuthenticationEntryPoint The handler for 401 Unauthorized errors.
   * @param customAccessDeniedHandler The handler for 403 Forbidden errors.
   * @param corsConfigurationSource The source for CORS policy configuration.
   * @param jwtAuthenticationFilter The custom filter for JWT validation.
   * @return The built {@link SecurityFilterChain}.
   * @throws Exception If an error occurs during the configuration of the security filters.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
      CustomAccessDeniedHandler customAccessDeniedHandler,
      CorsConfigurationSource corsConfigurationSource,
      JwtAuthenticationFilter jwtAuthenticationFilter)
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
                auth.requestMatchers(HttpMethod.POST, "/api/auth/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/customers/me")
                    .hasAnyRole(UserRole.CUSTOMER.name(), UserRole.ADMIN.name())
                    .requestMatchers(HttpMethod.GET, "/api/customers/*/validate")
                    .hasRole(UserRole.SERVICE.name())
                    .requestMatchers(HttpMethod.POST, "/api/customers/*/validate-pin")
                    .hasRole(UserRole.SERVICE.name())
                    .requestMatchers(HttpMethod.GET, "/api/customers/*")
                    .hasAnyRole(UserRole.ADMIN.name(), UserRole.SERVICE.name())
                    .anyRequest()
                    .denyAll())
        .authenticationProvider(provider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
      @Value("${app.cors.max-age:3600}") long maxAge) {
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
   * Configures the {@link DaoAuthenticationProvider} to use the custom {@link UserDetailsService}
   * and the configured password encoder.
   *
   * @return The configured {@link DaoAuthenticationProvider}.
   */
  @Bean
  public DaoAuthenticationProvider provider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  /**
   * Exposes the {@link AuthenticationManager} bean from the global configuration.
   *
   * @param config The {@link AuthenticationConfiguration} used to retrieve the manager.
   * @return The {@link AuthenticationManager} instance.
   * @throws Exception If an error occurs during retrieval.
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Creates a {@link SecretKey} for JWT signing and validation.
   *
   * <p>It decodes the Base64-encoded secret provided in the application properties.
   *
   * @param jwtSecret The Base64 encoded secret key string.
   * @return A {@link SecretKey} instance for HMAC-SHA algorithms.
   */
  @Bean
  public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String jwtSecret) {
    String cleanSecret = jwtSecret.trim();
    byte[] keyBytes = Decoders.BASE64URL.decode(cleanSecret);

    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Provides a {@link PasswordEncoder} bean used to hash passwords securely.
   *
   * <p>This implementation uses {@link BCryptPasswordEncoder}, which applies a strong hashing
   * algorithm with built-in salt to protect against brute-force and rainbow table attacks.
   *
   * @return a BCrypt-based password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

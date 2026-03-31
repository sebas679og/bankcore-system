package com.bankcore.customers.services;

import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.repository.UserRepository;
import com.bankcore.customers.utils.enums.CustomerStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of {@link UserDetailsService} to integrate the application's persistent
 * user data with Spring Security.
 *
 * <p>This service is responsible for retrieving user information from the database and mapping the
 * internal {@link UserEntity} state to a {@link UserDetails} object that the security framework can
 * process for authentication and authorization.
 *
 * @author BankCore Team - Cristian Ortiz
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Locates the user based on their unique identifier (UUID).
   *
   * @param uuid The string representation of the user's UUID.
   * @return A fully populated {@link UserDetails} object for the security context.
   * @throws UsernameNotFoundException If the user does not exist or the UUID format is invalid.
   */
  @Override
  public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {

    UserEntity userEntity =
        userRepository
            .findById(UUID.fromString(uuid))
            .orElseThrow(
                () -> {
                  log.warn("User not found with uuid: {}", uuid);
                  return new UsernameNotFoundException("User not found");
                });

    return buildUserDetails(userEntity);
  }

  /**
   * Maps the internal {@link UserEntity} domain model to a Spring Security {@link UserDetails}.
   *
   * <p>Logic applied for account status:
   *
   * <ul>
   *   <li><b>Disabled:</b> If status is INACTIVE or PENDING_VERIFICATION.
   *   <li><b>Account Locked:</b> If status is BLOCKED.
   *   <li><b>Expirations:</b> Handled based on the ACTIVE state.
   * </ul>
   *
   * @param userEntity The persistent user entity from the database.
   * @return An immutable {@link User} object used by the authentication provider.
   */
  private UserDetails buildUserDetails(UserEntity userEntity) {

    boolean isActive = userEntity.getStatus() == CustomerStatus.ACTIVE;

    boolean disabled =
        userEntity.getStatus() == CustomerStatus.INACTIVE
            || userEntity.getStatus() == CustomerStatus.PENDING_VERIFICATION;
    boolean accountExpired = !isActive;
    boolean accountLocked = userEntity.getStatus() == CustomerStatus.BLOCKED;
    boolean credentialsExpired = !isActive;

    return User.builder()
        .username(String.valueOf(userEntity.getId()))
        .password(userEntity.getPassword())
        .authorities(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()))
        .accountExpired(accountExpired)
        .accountLocked(accountLocked)
        .credentialsExpired(credentialsExpired)
        .disabled(disabled)
        .build();
  }
}

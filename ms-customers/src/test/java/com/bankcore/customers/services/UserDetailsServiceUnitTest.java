package com.bankcore.customers.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bankcore.customers.DataProvider;
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.repository.UserRepository;
import com.bankcore.customers.utils.enums.CustomerStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  @Test
  void shouldThrow_whenUserNotFound() {
    String testUUID = UUID.randomUUID().toString();
    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(testUUID))
        .isInstanceOf(UsernameNotFoundException.class);

    verify(userRepository, times(1)).findById(UUID.fromString(testUUID));
  }

  @Test
  void shouldThrow_whenUUIDFormatIsInvalid() {
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("no-es-un-uuid"))
        .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldMapUsernameAndPassword_correctly() {
    UserEntity user = DataProvider.createMockUser();
    String testUUID = UUID.randomUUID().toString();

    user.setId(UUID.fromString(testUUID));
    user.setPassword("$2a$10$hashedPasswordExample");

    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUUID);

    assertThat(result.getUsername()).isEqualTo(testUUID);
    assertThat(result.getPassword()).isEqualTo("$2a$10$hashedPasswordExample");
  }

  @Test
  void shouldMapRole_correctly() {
    UserEntity user = DataProvider.createMockUser();
    String testUUID = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUUID));

    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUUID);

    assertThat(result.getAuthorities())
        .hasSize(1)
        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
  }

  @Test
  void shouldBeFullyEnabled_whenActive() {
    UserEntity user = DataProvider.createMockUser();
    String testUUID = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUUID));

    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUUID);

    assertThat(result.isEnabled()).isTrue();
    assertThat(result.isAccountNonExpired()).isTrue();
    assertThat(result.isAccountNonLocked()).isTrue();
    assertThat(result.isCredentialsNonExpired()).isTrue();
  }

  @Test
  void shouldBeDisabledAndExpired_whenInactive() {

    UserEntity user = DataProvider.createMockUser();
    String testUUID = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUUID));
    user.setStatus(CustomerStatus.INACTIVE);

    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUUID);

    assertThat(result.isEnabled()).isFalse();
    assertThat(result.isAccountNonExpired()).isFalse();
    assertThat(result.isCredentialsNonExpired()).isFalse();
    // INACTIVE is not BLOCKED, should not be locked
    assertThat(result.isAccountNonLocked()).isTrue();
  }

  @Test
  void shouldBeDisabledAndExpired_whenPendingVerification() {

    UserEntity user = DataProvider.createMockUser();
    String testUUID = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUUID));
    user.setStatus(CustomerStatus.PENDING_VERIFICATION);

    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUUID);

    assertThat(result.isEnabled()).isFalse();
    assertThat(result.isAccountNonExpired()).isFalse();
    assertThat(result.isCredentialsNonExpired()).isFalse();
    assertThat(result.isAccountNonLocked()).isTrue();
  }

  @Test
  void shouldBeLockedAndExpired_whenBlocked() {

    UserEntity user = DataProvider.createMockUser();
    String testUUID = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUUID));
    user.setStatus(CustomerStatus.BLOCKED);

    when(userRepository.findById(UUID.fromString(testUUID))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUUID);

    assertThat(result.isAccountNonLocked()).isFalse();
    assertThat(result.isAccountNonExpired()).isFalse();
    assertThat(result.isCredentialsNonExpired()).isFalse();
    // BLOCKED is not INACTIVE and is not PENDING_VERIFICATION, should be enabled but locked
    assertThat(result.isEnabled()).isTrue();
  }
}

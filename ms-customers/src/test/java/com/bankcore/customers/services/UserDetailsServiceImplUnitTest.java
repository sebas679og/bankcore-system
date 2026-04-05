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
class UserDetailsServiceImplUnitTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  @Test
  void shouldThrow_whenUserNotFound() {
    String testUuid = UUID.randomUUID().toString();
    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(testUuid))
        .isInstanceOf(UsernameNotFoundException.class);

    verify(userRepository, times(1)).findById(UUID.fromString(testUuid));
  }

  @Test
  void shouldThrow_whenUuidFormatIsInvalid() {
    assertThatThrownBy(() -> userDetailsService.loadUserByUsername("no-es-un-uuid"))
        .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldMapUsernameAndPassword_correctly() {
    UserEntity user = DataProvider.createMockUser();
    String testUuid = UUID.randomUUID().toString();

    user.setId(UUID.fromString(testUuid));
    user.setPassword("$2a$10$hashedPasswordExample");

    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUuid);

    assertThat(result.getUsername()).isEqualTo(testUuid);
    assertThat(result.getPassword()).isEqualTo("$2a$10$hashedPasswordExample");
  }

  @Test
  void shouldMapRole_correctly() {
    UserEntity user = DataProvider.createMockUser();
    String testUuid = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUuid));

    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUuid);

    assertThat(result.getAuthorities())
        .hasSize(1)
        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
  }

  @Test
  void shouldBeFullyEnabled_whenActive() {
    UserEntity user = DataProvider.createMockUser();
    String testUuid = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUuid));

    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUuid);

    assertThat(result.isEnabled()).isTrue();
    assertThat(result.isAccountNonExpired()).isTrue();
    assertThat(result.isAccountNonLocked()).isTrue();
    assertThat(result.isCredentialsNonExpired()).isTrue();
  }

  @Test
  void shouldBeDisabledAndExpired_whenInactive() {

    UserEntity user = DataProvider.createMockUser();
    String testUuid = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUuid));
    user.setStatus(CustomerStatus.INACTIVE);

    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUuid);

    assertThat(result.isEnabled()).isFalse();
    assertThat(result.isAccountNonExpired()).isFalse();
    assertThat(result.isCredentialsNonExpired()).isFalse();
    // INACTIVE is not BLOCKED, should not be locked
    assertThat(result.isAccountNonLocked()).isTrue();
  }

  @Test
  void shouldBeDisabledAndExpired_whenPendingVerification() {

    UserEntity user = DataProvider.createMockUser();
    String testUuid = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUuid));
    user.setStatus(CustomerStatus.PENDING_VERIFICATION);

    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUuid);

    assertThat(result.isEnabled()).isFalse();
    assertThat(result.isAccountNonExpired()).isFalse();
    assertThat(result.isCredentialsNonExpired()).isFalse();
    assertThat(result.isAccountNonLocked()).isTrue();
  }

  @Test
  void shouldBeLockedAndExpired_whenBlocked() {

    UserEntity user = DataProvider.createMockUser();
    String testUuid = UUID.randomUUID().toString();
    user.setId(UUID.fromString(testUuid));
    user.setStatus(CustomerStatus.BLOCKED);

    when(userRepository.findById(UUID.fromString(testUuid))).thenReturn(Optional.of(user));

    UserDetails result = userDetailsService.loadUserByUsername(testUuid);

    assertThat(result.isAccountNonLocked()).isFalse();
    assertThat(result.isAccountNonExpired()).isFalse();
    assertThat(result.isCredentialsNonExpired()).isFalse();
    // BLOCKED is not INACTIVE and is not PENDING_VERIFICATION, should be enabled but locked
    assertThat(result.isEnabled()).isTrue();
  }
}

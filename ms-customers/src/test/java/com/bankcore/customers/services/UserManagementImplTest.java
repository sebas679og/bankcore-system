package com.bankcore.customers.services;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bankcore.customers.dto.requests.PinValidateRequest;
import com.bankcore.customers.dto.responses.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bankcore.customers.DataProvider;
import com.bankcore.customers.dto.requests.RegisterRequest;
import com.bankcore.customers.exceptions.ResourceConflictException;
import com.bankcore.customers.exceptions.UserProfileNotFoundException;
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.repository.UserRepository;
import com.bankcore.customers.utils.enums.CustomerStatus;
import com.bankcore.customers.utils.mappers.UserMapper;

@ExtendWith(MockitoExtension.class)
class UserManagementImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserManagementImpl userManagement;

    private final RegisterRequest request =
            RegisterRequest.builder()
                    .dni("1234567890")
                    .firstName("John")
                    .lastName("Doe")
                    .email("johndoe@email.com")
                    .password("Password123!")
                    .atmPin("1234")
                    .phone("+573001234567")
                    .address("123 Main St")
                    .build();

    @Test
    void registerCustomer_Success() {
        RegisterResponse expectedResponse =
                RegisterResponse.builder()
                        .id(java.util.UUID.randomUUID())
                        .dni(request.getDni())
                        .fullName(request.getFirstName() + " " + request.getLastName())
                        .email(request.getEmail())
                        .status(CustomerStatus.ACTIVE)
                        .createdDate(java.time.Instant.now())
                        .build();

        when(userRepository.existsByDni(request.getDni())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(passwordEncoder.encode(request.getAtmPin())).thenReturn("hashedAtmPin");
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toRegisterResponse(any(UserEntity.class)))
                .thenReturn(expectedResponse);

        RegisterResponse response = userManagement.registerCustomer(request);

        assertNotNull(response);
        assertEquals(expectedResponse.getDni(), response.getDni());
        assertEquals(expectedResponse.getEmail(), response.getEmail());

        verify(userRepository, times(1)).existsByDni(request.getDni());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(userMapper, times(1)).toRegisterResponse(any(UserEntity.class));
    }

    @Test
    void registerCustomer_Failure_DniAlreadyExists() {
        when(userRepository.existsByDni(request.getDni()))
                .thenReturn(true);

        ResourceConflictException exception =
                assertThrows(ResourceConflictException.class, () ->
                        userManagement.registerCustomer(request)
                );

        assertEquals("DNI already exists", exception.getMessage());

        verify(userRepository, times(1)).existsByDni(request.getDni());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toRegisterResponse(any());
    }

    @Test
    void registerCustomer_Failure_EmailAlreadyExists() {
        when(userRepository.existsByDni(request.getDni()))
                .thenReturn(false);

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        ResourceConflictException exception =
                assertThrows(ResourceConflictException.class, () ->
                        userManagement.registerCustomer(request)
                );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepository, times(1)).existsByDni(request.getDni());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toRegisterResponse(any());
    }

    @Test
    void shouldReturnProfileWhenUserExists() {

        UserEntity user = DataProvider.createMockUser();
        UUID id = UUID.randomUUID();
        UserProfileResponse response = UserProfileResponse.builder()
                .id(id.toString())
                .dni(user.getDni())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .status(user.getStatus().toString())
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserProfileResponse(user)).thenReturn(response);

        UserProfileResponse result = userManagement.getCurrentUserProfile(id.toString());

        assertEquals(response, result);
        verify(userRepository).findById(id);
        verify(userMapper).toUserProfileResponse(user);

    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userManagement.getCurrentUserProfile(null));
    }

    @Test
    void shouldThrowWhenIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> userManagement.getCurrentUserProfile(""));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(UserProfileNotFoundException.class, () -> userManagement.getCurrentUserProfile(id.toString()));
    }

    @Test
    void shouldReturnCustomerDetailsWhenCustomerExists() {
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        CustomerDetailsValidateResponse response =
                CustomerDetailsValidateResponse.builder()
                        .id(id)
                        .dni("101226156")
                        .fullName("John Doe")
                        .email("johndoe@test.com")
                        .status(CustomerStatus.ACTIVE)
                        .build();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(entity));

        when(userMapper.toCustomerDetailsValidateResponse(entity))
                .thenReturn(response);

        CustomerDetailsValidateResponse result =
                userManagement.getDetailsCustomer(id);

        assertEquals(response, result);

        verify(userRepository).findById(id);
        verify(userMapper).toCustomerDetailsValidateResponse(entity);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () ->
                userManagement.getDetailsCustomer(id));

        verify(userRepository).findById(id);
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldReturnTrueWhenCustomerExistsAndIsActive() {

        UUID id = UUID.randomUUID();

        UserEntity entity = new UserEntity();
        entity.setStatus(CustomerStatus.ACTIVE);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(entity));

        CustomerValidateResponse result =
                userManagement.getCustomerIsActive(id);

        assertTrue(result.isExists());
        assertTrue(result.isActive());
        assertEquals(id, result.getCustomerId());

        verify(userRepository).findById(id);
    }

    @Test
    void shouldReturnInactiveWhenCustomerExistsButNotActive() {

        UUID id = UUID.randomUUID();

        UserEntity entity = new UserEntity();
        entity.setStatus(CustomerStatus.INACTIVE);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(entity));

        CustomerValidateResponse result =
                userManagement.getCustomerIsActive(id);

        assertTrue(result.isExists());
        assertFalse(result.isActive());
    }

    @Test
    void shouldReturnFalseWhenCustomerDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        CustomerValidateResponse result =
                userManagement.getCustomerIsActive(id);

        assertFalse(result.isExists());
        assertFalse(result.isActive());
    }

    @Test
    void shouldReturnValidTrueWhenPinMatches() {

        UUID customerId = UUID.randomUUID();

        PinValidateRequest request = PinValidateRequest.builder()
                .pin("1234")
                .build();

        UserEntity user = new UserEntity();
        user.setAtmPin("encoded-pin");

        when(userRepository.findById(customerId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encoded-pin")).thenReturn(true);

        PinValidateResponse response = userManagement
                .getPinValidateCustomer(request, customerId);

        assertTrue(response.isExists());
        assertTrue(response.isValid());

        verify(userRepository).findById(customerId);
        verify(passwordEncoder).matches("1234", "encoded-pin");
    }

    @Test
    void shouldReturnValidFalseWhenPinDoesNotMatch() {

        UUID customerId = UUID.randomUUID();

        PinValidateRequest request = PinValidateRequest.builder()
                .pin("9999")
                .build();

        UserEntity user = new UserEntity();
        user.setAtmPin("encoded-pin");

        when(userRepository.findById(customerId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("9999", "encoded-pin")).thenReturn(false);

        PinValidateResponse response = userManagement
                .getPinValidateCustomer(request, customerId);

        assertTrue(response.isExists());
        assertFalse(response.isValid());

        verify(userRepository).findById(customerId);
        verify(passwordEncoder).matches("9999", "encoded-pin");
    }

    @Test
    void shouldReturnExistsFalseWhenUserNotFound() {

        UUID customerId = UUID.randomUUID();

        PinValidateRequest request = PinValidateRequest.builder()
                .pin("1234")
                .build();

        when(userRepository.findById(customerId)).thenReturn(Optional.empty());

        PinValidateResponse response = userManagement
                .getPinValidateCustomer(request, customerId);

        assertFalse(response.isExists());
        assertFalse(response.isValid());

        verify(userRepository).findById(customerId);
        verifyNoInteractions(passwordEncoder);
    }
}
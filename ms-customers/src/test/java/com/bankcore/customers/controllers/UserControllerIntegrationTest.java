package com.bankcore.customers.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bankcore.customers.AbstractIntegrationTest;
import com.bankcore.customers.DataProvider;
import com.bankcore.customers.dto.requests.LoginRequest;
import com.bankcore.customers.dto.requests.RegisterRequest;
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.repository.UserRepository;
import com.bankcore.customers.utils.enums.CustomerStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the UserController, covering both registration and login functionalities.
 */
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;

  private final RegisterRequest firstsUserRegister =
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

  @BeforeEach
  void setUp() throws Exception {
    userRepository.deleteAll();
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstsUserRegister)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(
            result -> {
              String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
              UUID.fromString(id);
            })
        .andExpect(jsonPath("$.dni").value(firstsUserRegister.getDni()))
        .andExpect(jsonPath("$.email").value(firstsUserRegister.getEmail()))
        .andExpect(
            jsonPath("$.fullName")
                .value(firstsUserRegister.getFirstName() + " " + firstsUserRegister.getLastName()))
        .andExpect(jsonPath("$.status").value(CustomerStatus.ACTIVE.name()))
        .andExpect(jsonPath("$.createdDate").exists())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Test
  void registerDniAlreadyExists() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstsUserRegister)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void registerEmailAlreadyExists() throws Exception {
    RegisterRequest registerRequestWithSameEmail =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email(firstsUserRegister.getEmail()) // Same email as the first user
            .password("Password123!")
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestWithSameEmail)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void validationFieldsNull() throws Exception {
    RegisterRequest invalidRegisterRequest =
        RegisterRequest.builder()
            .dni(null)
            .firstName(null)
            .lastName(null)
            .email(null)
            .password(null)
            .atmPin(null)
            .phone(null)
            .address(null)
            .build();

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRegisterRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void validationFieldsBlanks() throws Exception {
    RegisterRequest invalidRegisterRequest =
        RegisterRequest.builder()
            .dni("")
            .firstName("")
            .lastName("")
            .email("")
            .password("")
            .atmPin("")
            .phone("")
            .address("")
            .build();

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRegisterRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void validationFieldEmailInvalid() throws Exception {
    RegisterRequest invalidRegisterRequest =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("invalid-email") // Invalid email format
            .password("Password123!")
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRegisterRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void validationFieldAtmPinInvalid() throws Exception {
    RegisterRequest invalidRegisterPinMinimunSize =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password123!")
            .atmPin("12") // Invalid ATM PIN (less than 4 digits)
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPinMaximumSize =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password123!")
            .atmPin("12345") // Invalid ATM PIN (more than 4 digits)
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPinLetters =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password123!")
            .atmPin("12mp") // Invalid ATM PIN (contains letters)
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPinSameDigits =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password123!")
            .atmPin("3333") // Invalid ATM PIN (same digits)
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    List<RegisterRequest> invalidRequests =
        List.of(
            invalidRegisterPinMinimunSize,
            invalidRegisterPinMaximumSize,
            invalidRegisterPinLetters,
            invalidRegisterPinSameDigits);

    for (RegisterRequest request : invalidRequests) {

      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
          .andExpect(jsonPath("$.description").exists());
    }
  }

  @Test
  void validationFieldPasswordInvalid() throws Exception {
    RegisterRequest invalidRegisterPasswordMinimunSize =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Pass1!") // Invalid password (less than 8 characters)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPasswordNoUppercase =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("password12!") // Invalid password (no uppercase letters)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPasswordNoLowercase =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("PASSWORD123!") // Invalid password (no lowercase letters)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPasswordNoDigit =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password#!") // Invalid password (no digits)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPasswordNoSpecialChar =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password123456") // Invalid password (no special characters)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPasswordMaximumSize =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("PasswordLarge1234567890#!&") // Invalid password (more than 20 characters)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    RegisterRequest invalidRegisterPasswordWhiteSpaces =
        RegisterRequest.builder()
            .dni("0987654321")
            .firstName("Jane")
            .lastName("Smith")
            .email("janesmith@email.com")
            .password("Password 123!") // Invalid password (contains whitespace)
            .atmPin("4321")
            .phone("+573009876543")
            .address("456 Elm St")
            .build();

    List<RegisterRequest> invalidRequests =
        List.of(
            invalidRegisterPasswordMinimunSize,
            invalidRegisterPasswordNoUppercase,
            invalidRegisterPasswordNoLowercase,
            invalidRegisterPasswordNoDigit,
            invalidRegisterPasswordNoSpecialChar,
            invalidRegisterPasswordMaximumSize,
            invalidRegisterPasswordWhiteSpaces);

    for (RegisterRequest request : invalidRequests) {

      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
          .andExpect(jsonPath("$.description").exists());
    }
  }

  @Test
  void shouldReturn200_whenActiveUserLogsIn() throws Exception {
    UserEntity user = DataProvider.createMockUser();
    String password = user.getPassword();

    user.setPassword(passwordEncoder.encode(password));

    UserEntity saved = userRepository.save(user);

    LoginRequest request =
        LoginRequest.builder().email(saved.getEmail()).password(password).build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").isNumber())
        .andExpect(jsonPath("$.customerId").value(saved.getId().toString()));
  }

  @Test
  void shouldReturn401_whenPasswordIsWrong() throws Exception {

    UserEntity user = DataProvider.createMockUser();
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    UserEntity saved = userRepository.save(user);

    LoginRequest request =
        LoginRequest.builder().email(saved.getEmail()).password("wrongPassword123!").build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenEmailNotFound() throws Exception {
    UserEntity user = DataProvider.createMockUser();
    String password = user.getPassword();

    user.setPassword(passwordEncoder.encode(password));
    userRepository.save(user);

    LoginRequest request =
        LoginRequest.builder().email("random@email.com").password(password).build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenUserIsBlocked() throws Exception {
    UserEntity user = DataProvider.createMockUser();
    user.setStatus(CustomerStatus.BLOCKED);
    String password = user.getPassword();

    user.setPassword(passwordEncoder.encode(password));
    UserEntity saved = userRepository.save(user);

    LoginRequest request =
        LoginRequest.builder().email(saved.getEmail()).password(password).build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenUserIsInactive() throws Exception {
    UserEntity user = DataProvider.createMockUser();
    user.setStatus(CustomerStatus.INACTIVE);
    String password = user.getPassword();

    user.setPassword(passwordEncoder.encode(password));
    UserEntity saved = userRepository.save(user);

    LoginRequest request =
        LoginRequest.builder().email(saved.getEmail()).password(password).build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401_whenUserIsPendingVerification() throws Exception {
    UserEntity user = DataProvider.createMockUser();
    user.setStatus(CustomerStatus.PENDING_VERIFICATION);
    String password = user.getPassword();

    user.setPassword(passwordEncoder.encode(password));
    UserEntity saved = userRepository.save(user);

    LoginRequest request =
        LoginRequest.builder().email(saved.getEmail()).password(password).build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn400_whenEmailIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    { "email": "", "password": "Secure@123" }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenEmailIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    { "email": "not-an-email", "password": "Secure@123" }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenPasswordIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    { "email": "customer@bankcore.com", "password": "" }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenPasswordIsTooShort() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    { "email": "customer@bankcore.com", "password": "Ab@1" }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenPasswordHasNoSpecialChar() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    { "email": "customer@bankcore.com", "password": "Secure1234" }
                                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400_whenBodyIsEmpty() throws Exception {
    mockMvc
        .perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }
}

package com.bankcore.customers.controllers;

import com.bankcore.customers.dto.requests.PinValidateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.bankcore.customers.AbstractIntegrationTest;
import com.bankcore.customers.DataProvider;
import com.bankcore.customers.model.UserEntity;
import com.bankcore.customers.repository.UserRepository;
import com.bankcore.customers.services.UserManagementImpl;
import com.bankcore.customers.utils.enums.CustomerStatus;
import com.bankcore.customers.utils.enums.UserRole;

import java.util.List;


@Transactional
@ActiveProfiles("test")
public class ProfileControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserManagementImpl userManagement;

    private UserEntity savedUser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void init() {
        UserEntity user = DataProvider.createMockUser();

        user.setAtmPin(passwordEncoder.encode(user.getAtmPin()));

        savedUser = userRepository.save(user);
    }

    @Test
    void shouldReturnProfileWhenAuthorized() throws Exception {
        mockMvc.perform(get("/api/customers/me")
                        .with(user(savedUser.getId().toString())
                                .roles(DataProvider.CUSTOMER_ROLE))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Perez"))
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.phone").value("3001234567"))
                .andExpect(jsonPath("$.address").value("Bogotá"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists());


    }

    @Test
    @WithMockUser(username = DataProvider.UUID, roles = DataProvider.CUSTOMER_ROLE)
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/me"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = DataProvider.UUID, roles = "USER")
    void shouldReturn403WhenWrongRole() throws Exception {

        mockMvc.perform(get("/api/customers/me"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/customers/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.ADMIN_ROLE)
    void shouldReturn200WhenAdminRequestsCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.dni").value(savedUser.getDni()))
                .andExpect(jsonPath("$.fullName").value(String.join(" ", savedUser.getFirstName(), savedUser.getLastName())))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.status").value(savedUser.getStatus().toString()));
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn200WhenServiceRequestsCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.dni").value(savedUser.getDni()))
                .andExpect(jsonPath("$.fullName").value(String.join(" ", savedUser.getFirstName(), savedUser.getLastName())))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.status").value(savedUser.getStatus().toString()));
    }

    @Test
    @WithMockUser(roles = DataProvider.CUSTOMER_ROLE)
    void shouldReturn403WhenCustomerRequestsCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", savedUser.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.ADMIN_ROLE)
    void shouldReturn400WhenServiceRequestsCustomerPathInvalidFormat() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", DataProvider.INVALID_UUID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn404WhenServiceRequestsCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", DataProvider.UUID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shouldReturn401WhenServiceRequestsCustomerNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", savedUser.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn200WhenServiceRoleCustomerExist() throws Exception {

        mockMvc.perform(get("/api/customers/{id}/validate", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(true));

    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn200WhenServiceRoleCustomerNotExist() throws Exception {

        mockMvc.perform(get("/api/customers/{id}/validate", DataProvider.UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(DataProvider.UUID))
                .andExpect(jsonPath("$.exists").value(false))
                .andExpect(jsonPath("$.active").value(false));

    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn200WhenServiceRoleCustomerInactive() throws Exception {
        userRepository.deleteAll();
        UserEntity user = UserEntity.builder()
                .dni("12345678")
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .password("Password123!")
                .atmPin("1234")
                .phone("3001234567")
                .address("Bogotá")
                .role(UserRole.CUSTOMER)
                .status(CustomerStatus.INACTIVE)
                .build();

        savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/customers/{id}/validate", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(false));

    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn200WhenServiceRoleCustomerBlocked() throws Exception {
        userRepository.deleteAll();
        UserEntity user = UserEntity.builder()
                .dni("12345678")
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .password("Password123!")
                .atmPin("1234")
                .phone("3001234567")
                .address("Bogotá")
                .role(UserRole.CUSTOMER)
                .status(CustomerStatus.BLOCKED)
                .build();

        savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/customers/{id}/validate", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(false));

    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn200WhenServiceRoleCustomerPendingVerification() throws Exception {
        userRepository.deleteAll();
        UserEntity user = UserEntity.builder()
                .dni("12345678")
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .password("Password123!")
                .atmPin("1234")
                .phone("3001234567")
                .address("Bogotá")
                .role(UserRole.CUSTOMER)
                .status(CustomerStatus.PENDING_VERIFICATION)
                .build();

        savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/customers/{id}/validate", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(false));

    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn400WhenServiceRolePathFormatIncorrect() throws Exception {

        mockMvc.perform(get("/api/customers/{id}/validate", DataProvider.INVALID_UUID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());

    }

    @Test
    @WithMockUser(roles = DataProvider.ADMIN_ROLE)
    void shouldReturn403WhenAdminAccessValidateEndpoint() throws Exception {

        mockMvc.perform(get("/api/customers/{id}/validate", savedUser.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());

    }

    @Test
    void shouldReturn401WhenNotAuthenticatedValidate() throws Exception {

        mockMvc.perform(get("/api/customers/{id}/validate", savedUser.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());

    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturnTrueWhenProvidedPinMatchesStoredPin() throws Exception{

        PinValidateRequest request = DataProvider.createMockPinValidate(DataProvider.createMockUser().getAtmPin());

        mockMvc.perform(post("/api/customers/{id}/validate-pin", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturnFalseWhenProvidedPinDoesNotMatchStoredPin() throws Exception{

        PinValidateRequest request = DataProvider.createMockPinValidate("4590");

        mockMvc.perform(post("/api/customers/{id}/validate-pin", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturnFalseWhenUserDoesNotExist() throws Exception{

        PinValidateRequest request = DataProvider.createMockPinValidate("4576");

        mockMvc.perform(post("/api/customers/{id}/validate-pin", DataProvider.UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists").value(false))
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void shouldReturn401WhenRequestIsNotAuthenticated() throws Exception{

        PinValidateRequest request = DataProvider.createMockPinValidate("3176");

        mockMvc.perform(post("/api/customers/{id}/validate-pin", DataProvider.UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.CUSTOMER_ROLE)
    void shouldReturn403WhenUserDoesNotHaveRequiredRole() throws Exception{

        PinValidateRequest request = DataProvider.createMockPinValidate("5621");

        mockMvc.perform(post("/api/customers/{id}/validate-pin", DataProvider.UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn400WhenCustomerIdIsInvalidUuid() throws Exception{

        PinValidateRequest request = DataProvider.createMockPinValidate("3412");

        mockMvc.perform(post("/api/customers/{id}/validate-pin", DataProvider.INVALID_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(roles = DataProvider.SERVICE_ROLE)
    void shouldReturn400WhenPinRequestValidationFails() throws Exception{

        PinValidateRequest pinMinimunSize = DataProvider.createMockPinValidate("12");
        PinValidateRequest pinMaximumSize = DataProvider.createMockPinValidate("12345");
        PinValidateRequest pinLetters = DataProvider.createMockPinValidate("12mp");
        PinValidateRequest pinSameDigits = DataProvider.createMockPinValidate("3333");
        PinValidateRequest pinBlankSpace = DataProvider.createMockPinValidate("");
        PinValidateRequest pinNull = PinValidateRequest.builder()
                .pin(null)
                .build();

        List<PinValidateRequest> invalidRequests = List.of(
                pinMinimunSize,
                pinMaximumSize,
                pinLetters,
                pinSameDigits,
                pinBlankSpace,
                pinNull
        );

        for (PinValidateRequest request : invalidRequests){

            mockMvc.perform(post("/api/customers/{id}/validate-pin", DataProvider.INVALID_UUID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.description").exists());

        }
    }
}

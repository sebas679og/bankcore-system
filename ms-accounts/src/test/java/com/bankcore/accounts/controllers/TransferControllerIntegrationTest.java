package com.bankcore.accounts.controllers;

import com.bankcore.accounts.AbstractIntegrationTest;
import com.bankcore.accounts.AccountDataProvider;
import com.bankcore.accounts.TransferAccountProvider;
import com.bankcore.accounts.dto.requests.TransactionRequest;
import com.bankcore.accounts.dto.requests.TransferRequest;
import com.bankcore.accounts.integrations.client.CustomerClient;
import com.bankcore.accounts.integrations.dto.request.PinValidateRequest;
import com.bankcore.accounts.integrations.dto.responses.CustomerDetailsResponse;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.integrations.dto.responses.PinValidateResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.repositories.TransferRepository;
import com.bankcore.accounts.utils.enums.CustomerStatus;
import com.bankcore.accounts.utils.enums.TransferStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.nullValue;

public class TransferControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerClient customerClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransferRepository transferRepository;

    private final BigDecimal initialBalance = BigDecimal.valueOf(10000.00);
    private AccountEntity sourceAccount;
    private AccountEntity destinationAccount;

    @BeforeEach
    public void setUp() {
        Mockito.reset(customerClient);
        transferRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        sourceAccount = accountRepository.save(AccountDataProvider.createMockAccount(initialBalance));
        destinationAccount = accountRepository.save(AccountDataProvider.createMockAccount());
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean
        CustomerClient customerClient() {
            return Mockito.mock(CustomerClient.class);
        }
    }

    @Test
    public void shouldRegisterTransferBetweenAccountsSuccessfully() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(BigDecimal.valueOf(1000.00))
                .description("test-transfer")
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        CustomerDetailsResponse customerResponse = new CustomerDetailsResponse(
                destinationAccount.getCustomerId(),
                "1234567890",
                "John Doe",
                "johndoe@email.com",
                CustomerStatus.ACTIVE
        );

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        Mockito.when(customerClient.getCustomerDetailsById(any(UUID.class)))
                .thenReturn(customerResponse);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").exists())
                .andExpect(jsonPath("$.status").value(TransferStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.sourceAccount").value(sourceAccount.getAccountNumber()))
                .andExpect(jsonPath("$.destinationAccount").value(destinationAccount.getAccountNumber()))
                .andExpect(jsonPath("$.beneficiaryName").value(customerResponse.fullName()))
                .andExpect(jsonPath("$.amount").value(request.getAmount()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.fee").exists())
                .andExpect(jsonPath("$.totalDebited").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void shouldRegisterTransferSuccessfullyWhenDescriptionIsNull() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(BigDecimal.valueOf(1000.00))
                .description(null)
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        CustomerDetailsResponse customerResponse = new CustomerDetailsResponse(
                destinationAccount.getCustomerId(),
                "1234567890",
                "John Doe",
                "johndoe@email.com",
                CustomerStatus.ACTIVE
        );

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        Mockito.when(customerClient.getCustomerDetailsById(any(UUID.class)))
                .thenReturn(customerResponse);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").exists())
                .andExpect(jsonPath("$.status").value(TransferStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.sourceAccount").value(sourceAccount.getAccountNumber()))
                .andExpect(jsonPath("$.destinationAccount").value(destinationAccount.getAccountNumber()))
                .andExpect(jsonPath("$.beneficiaryName").value(customerResponse.fullName()))
                .andExpect(jsonPath("$.amount").value(request.getAmount()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.fee").exists())
                .andExpect(jsonPath("$.totalDebited").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void shouldRegisterTransferToExternalAccountSuccessfully() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(AccountDataProvider.generateIban())
                .amount(BigDecimal.valueOf(1000.00))
                .description("test-transfer")
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").exists())
                .andExpect(jsonPath("$.status").value(TransferStatus.PENDING.name()))
                .andExpect(jsonPath("$.sourceAccount").value(sourceAccount.getAccountNumber()))
                .andExpect(jsonPath("$.destinationAccount").value(request.getDestinationAccountNumber()))
                .andExpect(jsonPath("$.beneficiaryName").value(nullValue()))
                .andExpect(jsonPath("$.amount").value(request.getAmount()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.fee").exists())
                .andExpect(jsonPath("$.totalDebited").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void shouldRegisterTransferToExternalAccountSuccessfullyWhenDescriptionIsNull() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(AccountDataProvider.generateIban())
                .amount(BigDecimal.valueOf(1000.00))
                .description(null)
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId").exists())
                .andExpect(jsonPath("$.status").value(TransferStatus.PENDING.name()))
                .andExpect(jsonPath("$.sourceAccount").value(sourceAccount.getAccountNumber()))
                .andExpect(jsonPath("$.destinationAccount").value(request.getDestinationAccountNumber()))
                .andExpect(jsonPath("$.beneficiaryName").value(nullValue()))
                .andExpect(jsonPath("$.amount").value(request.getAmount()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.fee").exists())
                .andExpect(jsonPath("$.totalDebited").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void SourceAccountNotFoundException() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(UUID.randomUUID())
                .destinationAccountNumber(AccountDataProvider.generateIban())
                .amount(BigDecimal.valueOf(1000.00))
                .description("test-transfer")
                .pin("1234")
                .build();

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldThrowSameAccountTransferException() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(sourceAccount.getAccountNumber())
                .amount(BigDecimal.valueOf(1000.00))
                .description("test-transfer")
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldThrowInsufficientBalanceException() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(initialBalance.add(BigDecimal.valueOf(100.00)))
                .description("test-transfer")
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        CustomerDetailsResponse customerResponse = new CustomerDetailsResponse(
                destinationAccount.getCustomerId(),
                "1234567890",
                "John Doe",
                "johndoe@email.com",
                CustomerStatus.ACTIVE
        );

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        Mockito.when(customerClient.getCustomerDetailsById(any(UUID.class)))
                .thenReturn(customerResponse);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldFailWhenDestinationAccountIsInactive() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        AccountEntity accountInactive = accountRepository.save(AccountDataProvider.createMockAccountStatusInactive());

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(accountInactive.getAccountNumber())
                .amount(BigDecimal.valueOf(100.00))
                .description("test-transfer")
                .pin("1234")
                .build();

        PinValidateRequest requestPin = PinValidateRequest.builder().pin(request.getPin()).build();

        Mockito.when(customerClient.getCustomerById(customerId))
                .thenReturn(new CustomerResponse(customerId, true, true));

        Mockito.when(customerClient.validateCustomerPin(customerId, requestPin))
                .thenReturn(new PinValidateResponse(true));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldFailWhenIdFormatIsInvalid() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        String request = """
                {
                  "sourceAccountId": "550e8400-e29b-41d4-a716-44665544000Z",
                  "destinationAccountNumber": "ES2553907030769590566959",
                  "amount": 100.00,
                  "description": "test-transfer",
                  "pin": "1234"
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldFailWhenIbanIsInvalid() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(AccountDataProvider.INVALID_IBAN)
                .amount(BigDecimal.valueOf(100.00))
                .description("test-transfer")
                .pin("1234")
                .build();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldFailWhenAmountIsLessThanOne() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(AccountDataProvider.INVALID_IBAN)
                .amount(BigDecimal.valueOf(0.99))
                .description("test-transfer")
                .pin("1234")
                .build();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("CUSTOMER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldRegisterTransferWithPinInvalid() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest pinMinimumSize = TransferAccountProvider.createMockTransferRequest(sourceAccount.getId(),
                "12",
                destinationAccount.getAccountNumber()
        );

        TransferRequest pinMaximumSize = TransferAccountProvider.createMockTransferRequest(sourceAccount.getId(),
                "12345",
                destinationAccount.getAccountNumber()
        );

        TransferRequest pinLetters = TransferAccountProvider.createMockTransferRequest(sourceAccount.getId(),
                "12mp",
                destinationAccount.getAccountNumber()
        );


        TransferRequest pinBlankSpace = TransferAccountProvider.createMockTransferRequest(sourceAccount.getId(),
                "",
                destinationAccount.getAccountNumber()
        );

        TransferRequest pinNull = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(BigDecimal.valueOf(10000.00))
                .description("test-transfer")
                .pin(null)
                .build();

        List<TransferRequest> invalidRequests = List.of(
                pinMinimumSize,
                pinMaximumSize,
                pinLetters,
                pinBlankSpace,
                pinNull
        );


        for (TransferRequest request : invalidRequests) {

            mockMvc.perform(post("/api/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(user(customerId.toString()).roles("CUSTOMER")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.description").exists());
        }
    }

    @Test
    public void shouldReturn403WhenUserDoesNotHaveRequiredRole() throws Exception{
        UUID customerId = sourceAccount.getCustomerId();

        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(AccountDataProvider.INVALID_IBAN)
                .amount(BigDecimal.valueOf(0.99))
                .description("test-transfer")
                .pin("1234")
                .build();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(customerId.toString()).roles("ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void shouldReturn401WhenRequestIsNotAuthenticated() throws Exception{
        TransferRequest request = TransferRequest.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountNumber(AccountDataProvider.INVALID_IBAN)
                .amount(BigDecimal.valueOf(0.99))
                .description("test-transfer")
                .pin("1234")
                .build();

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
                .andExpect(jsonPath("$.description").exists());
    }
}

package com.bankcore.accounts.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bankcore.accounts.AbstractIntegrationTest;
import com.bankcore.accounts.AccountDataProvider;
import com.bankcore.accounts.TransactionDataProvider;
import com.bankcore.accounts.dto.requests.AccountRegisterRequest;
import com.bankcore.accounts.integrations.client.CustomerClient;
import com.bankcore.accounts.integrations.dto.responses.CustomerResponse;
import com.bankcore.accounts.models.AccountEntity;
import com.bankcore.accounts.models.TransactionEntity;
import com.bankcore.accounts.repositories.AccountRepository;
import com.bankcore.accounts.repositories.TransactionRepository;
import com.bankcore.accounts.services.complements.WithdrawalService;
import com.bankcore.accounts.utils.enums.AccountStatus;
import com.bankcore.accounts.utils.enums.AccountType;
import com.bankcore.accounts.utils.enums.CurrencyCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AccountControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CustomerClient customerClient;

  @Autowired private AccountRepository accountRepository;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private WithdrawalService withdrawalService;

  @BeforeEach
  void setUp() {
    Mockito.reset(customerClient);
    accountRepository.deleteAll();
    accountRepository.saveAll(
        List.of(
            AccountDataProvider.createMockAccount(
                UUID.fromString(AccountDataProvider.CUSTOMER_TEST_UUID), "Some alias"),
            AccountDataProvider.createMockAccount(
                UUID.fromString(AccountDataProvider.CUSTOMER_TEST_UUID), "Some alias"),
            AccountDataProvider.createMockAccount(
                UUID.fromString(AccountDataProvider.CUSTOMER_TEST_UUID), "Some alias")));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    CustomerClient customerClient() {
      return Mockito.mock(CustomerClient.class);
    }
  }

  @Test
  void shouldRegisterAccountSuccessfully() throws Exception {
    UUID customerId = UUID.randomUUID();
    Mockito.when(customerClient.getCustomerById(customerId))
        .thenReturn(new CustomerResponse(customerId, true, true));

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.CHECKING)
            .currency(CurrencyCode.EUR)
            .alias("MySavings")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.customerId").value(customerId.toString()))
        .andExpect(jsonPath("$.alias").value(request.getAlias()))
        .andExpect(jsonPath("$.accountType").value(request.getAccountType().name()))
        .andExpect(jsonPath("$.currency").value(request.getCurrency().name()));
  }

  @Test
  void shouldReturn404WhenCustomerNotFound() throws Exception {
    UUID customerId = UUID.randomUUID();
    when(customerClient.getCustomerById(customerId))
        .thenReturn(new CustomerResponse(customerId, false, false));

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.CHECKING)
            .currency(CurrencyCode.EUR)
            .alias("TestAlias")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturn403WhenCustomerInactive() throws Exception {
    UUID customerId = UUID.randomUUID();
    when(customerClient.getCustomerById(customerId))
        .thenReturn(new CustomerResponse(customerId, true, false));

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .alias("InactiveAlias")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturn401WhenCustomerNotAuthenticated() throws Exception {

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .alias("InactiveAlias")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturnConflictWhenAliasAlreadyExists() throws Exception {
    UUID customerId = UUID.randomUUID();
    when(customerClient.getCustomerById(customerId))
        .thenReturn(new CustomerResponse(customerId, true, true));

    AccountEntity existingAccount =
        AccountEntity.builder()
            .customerId(customerId)
            .alias("DuplicateAlias")
            .accountNumber("ES1234567890123456789012")
            .accountType(AccountType.CHECKING)
            .currency(CurrencyCode.EUR)
            .balance(BigDecimal.ZERO)
            .status(AccountStatus.ACTIVE)
            .dailyWithdrawalLimit(withdrawalService.resolveDailyLimit(AccountType.CHECKING))
            .build();
    accountRepository.save(existingAccount);

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .alias("DuplicateAlias")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturnUnprocessableEntityWhenCustomerAlreadyHasThreeAccounts() throws Exception {

    UUID customerId = UUID.randomUUID();

    when(customerClient.getCustomerById(customerId))
        .thenReturn(new CustomerResponse(customerId, true, true));

    for (int i = 1; i <= 3; i++) {
      AccountEntity account =
          AccountEntity.builder()
              .customerId(customerId)
              .alias("Account" + i)
              .accountNumber("ES12345678901234567890" + i)
              .accountType(AccountType.SAVINGS)
              .currency(CurrencyCode.EUR)
              .balance(BigDecimal.ZERO)
              .status(AccountStatus.ACTIVE)
              .dailyWithdrawalLimit(withdrawalService.resolveDailyLimit(AccountType.SAVINGS))
              .build();

      accountRepository.save(account);
    }

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .alias("FourthAccount")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .with(user(customerId.toString()).roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturnBadRequestForInvalidRequest() throws Exception {
    UUID customerId = UUID.randomUUID();

    AccountRegisterRequest request =
        AccountRegisterRequest.builder()
            .accountType(AccountType.SAVINGS)
            .currency(CurrencyCode.EUR)
            .alias("")
            .build();

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturnBadRequestForInvalidRequestEnums() throws Exception {

    UUID customerId = UUID.randomUUID();

    String invalidJson =
        """
                {
                    "accountType": "EnumNotPermited",
                    "currency": "EnumNotPermited",
                    "alias": "my-alias"
                }
                """;

    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void shouldReturnAccounts_whenCustomerAuthorizedAndActive() throws Exception {
    UUID customerId = UUID.fromString(AccountDataProvider.CUSTOMER_TEST_UUID);

    Mockito.when(customerClient.getCustomerById(customerId))
        .thenReturn(new CustomerResponse(customerId, true, true));

    mockMvc
        .perform(get("/api/accounts").with(user(customerId.toString()).roles("CUSTOMER")))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(3)));
  }

  @Test
  void shouldReturnNoAccounts_whenCustomerAuthorizedAndActive() throws Exception {
    UUID customerId = UUID.randomUUID();

    mockMvc
        .perform(get("/api/accounts").with(user(customerId.toString()).roles("CUSTOMER")))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void shouldReturn403_whenWrongRole() throws Exception {
    UUID customerId = UUID.fromString(AccountDataProvider.CUSTOMER_TEST_UUID);

    mockMvc
        .perform(get("/api/accounts").with(user(customerId.toString()).roles("ADMIN")))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.name").value("Forbidden"))
        .andExpect(jsonPath("$.code").value(403))
        .andExpect(
            jsonPath("$.description").value("You do not have permission to access this resource."))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn401_whenNotAuthenticated() throws Exception {
    mockMvc
        .perform(get("/api/accounts"))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.name").value("Unauthorized"))
        .andExpect(jsonPath("$.code").value(401))
        .andExpect(
            jsonPath("$.description").value("Authentication is required to access this resource."))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void getAccountDetails_whenValidOwner_returns200() throws Exception {
    UUID customerId = UUID.randomUUID();
    AccountEntity saved =
        accountRepository.save(AccountDataProvider.createMockAccount(customerId, "Random Alias"));

    mockMvc
        .perform(
            get("/api/accounts/{accountId}", saved.getId())
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(jsonPath("$.id").value(saved.getId().toString()))
        .andExpect(jsonPath("$.accountNumber").exists())
        .andExpect(jsonPath("$.accountNumber").isNotEmpty())
        .andExpect(jsonPath("$.customerId").value(customerId.toString()))
        .andExpect(jsonPath("$.accountType").value("SAVINGS"))
        .andExpect(jsonPath("$.currency").value("EUR"))
        .andExpect(jsonPath("$.balance").value(0))
        .andExpect(jsonPath("$.alias").exists())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
        .andExpect(jsonPath("$.lastTransactionAt").doesNotExist());
  }

  @Test
  void getAccountDetails_whenAccountHasLastTransaction_returnsLastTransactionAt() throws Exception {
    UUID customerId = UUID.randomUUID();
    AccountEntity savedAccount =
        accountRepository.save(AccountDataProvider.createMockAccount(customerId, "Random Alias"));

    TransactionEntity savedTransaction =
        transactionRepository.save(TransactionDataProvider.createMockTransaction(savedAccount));

    mockMvc
        .perform(
            get("/api/accounts/{accountId}", savedAccount.getId())
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.lastTransactionAt").value(savedTransaction.getCreatedAt().toString()));
  }

  @Test
  void getAccountDetails_whenAccountBelongsToAnotherCustomer_returns404() throws Exception {
    UUID customerId = UUID.randomUUID();
    AccountEntity saved =
        accountRepository.save(AccountDataProvider.createMockAccount(customerId, "Random Alias"));

    UUID anotherCustomerId = UUID.randomUUID();
    mockMvc
        .perform(
            get("/api/accounts/{accountId}", saved.getId())
                .with(user(anotherCustomerId.toString()).roles("CUSTOMER")))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void getAccountDetails_whenAccountDoesNotExist_returns404() throws Exception {
    UUID customerId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/api/accounts/{accountId}", UUID.randomUUID())
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAccountDetails_whenNoToken_returns401() throws Exception {
    UUID customerId = UUID.randomUUID();
    AccountEntity saved =
        accountRepository.save(AccountDataProvider.createMockAccount(customerId, "Random Alias"));
    mockMvc
        .perform(get("/api/accounts/{accountId}", saved.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getAccountDetails_whenInvalidAccountIdFormat_returns400() throws Exception {
    UUID customerId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/api/accounts/{accountId}", "not-a-uuid")
                .with(user(customerId.toString()).roles("CUSTOMER")))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }
}

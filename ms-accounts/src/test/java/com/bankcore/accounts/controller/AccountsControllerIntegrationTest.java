package com.bankcore.accounts.controller;

import com.bankcore.accounts.AbstractIntegrationTest;
import com.bankcore.accounts.DataProvider;
import com.bankcore.accounts.repositries.AccountRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;


import java.io.IOException;
import java.util.List;
import java.util.UUID;


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AccountsControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository repository;

    private static final MockWebServer mockWebServer = new MockWebServer();

    private static final UUID CUSTOMER_ID = UUID.fromString(DataProvider.CUSTOMER_TEST_UUID);

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("ms-customers.url", () -> mockWebServer.url("/").toString());
    }

    @BeforeEach
    void setupData() {
        repository.deleteAll();
        repository.saveAll(List.of(
                DataProvider.createDummyAccount("123456"),
                DataProvider.createDummyAccount("654321"),
                DataProvider.createDummyAccount("654123")
        ));
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockWebServer.shutdown();
    }

    private MockResponse buildCustomerResponse(boolean exists, boolean isActive) {
        return new MockResponse()
                .setBody("""
            {
                "customerId": "%s",
                "exist": %s,
                "active": %s
            }
            """.formatted(CUSTOMER_ID, exists, isActive))
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void shouldReturnAccounts_whenCustomerAuthorizedAndActive() throws Exception {
        mockWebServer.enqueue(buildCustomerResponse(true, true));

        mockMvc.perform(get("/api/accounts")
                        .with(user(CUSTOMER_ID.toString()).roles("CUSTOMER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldReturnNoAccounts_whenCustomerAuthorizedAndActive() throws Exception {
        repository.deleteAll(); // override del @BeforeEach para este caso específico
        mockWebServer.enqueue(buildCustomerResponse(true, true));

        mockMvc.perform(get("/api/accounts")
                        .with(user(CUSTOMER_ID.toString()).roles("CUSTOMER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturn403_whenCustomerInactive() throws Exception {
        mockWebServer.enqueue(buildCustomerResponse(true, false));

        mockMvc.perform(get("/api/accounts")
                        .with(user(CUSTOMER_ID.toString()).roles("CUSTOMER")))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Forbidden"))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.description").value("You do not have permission to access this resource."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn404_whenCustomerNotFound() throws Exception {
        mockWebServer.enqueue(buildCustomerResponse(false, false));

        mockMvc.perform(get("/api/accounts")
                        .with(user(CUSTOMER_ID.toString()).roles("CUSTOMER")))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Not Found"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.description").value("The authenticated client is not registered"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn502_whenExternalServiceFails() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        mockMvc.perform(get("/api/accounts")
                        .with(user(CUSTOMER_ID.toString()).roles("CUSTOMER")))
                .andDo(print())
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Bad Gateway"))
                .andExpect(jsonPath("$.code").value(502))
                .andExpect(jsonPath("$.description").value("Error communicating with Customer Service"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn403_whenWrongRole() throws Exception {
        mockMvc.perform(get("/api/accounts")
                        .with(user(CUSTOMER_ID.toString()).roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

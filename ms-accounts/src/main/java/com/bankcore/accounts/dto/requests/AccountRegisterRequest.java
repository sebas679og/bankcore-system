package com.bankcore.accounts.dto.requests;

import com.bankcore.accounts.utils.enums.AccountType;

import com.bankcore.accounts.utils.enums.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for account registration requests.
 * This class encapsulates the data required to create a new account, including the account type, currency, and alias.
 * It includes validation annotations to ensure that all required fields are provided.
 * @author BankCore Team - Sebastian Orjuela
 * @version 1.0
 */
@Getter
@Setter
@Builder
public class AccountRegisterRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Currency is required")
    private CurrencyCode currency;

    @NotBlank(message = "Alias is required")
    private String alias;
}

package com.example.bankingapi.dto.account;


import com.example.bankingapi.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountCreateRequest {

    @NotBlank(message = "Account number should not be blank")
    @Size(min = 1, max = 23, message = "Account number should be between 1 and 23 characters")
    private String accountNumber;

    @NotNull(message = "Account type should not be null")
    private AccountType accountType;

    @NotNull(message = "Balance must not be null")
    @PositiveOrZero(message = "Balance must be zero or positive")
    private BigDecimal balance;
}

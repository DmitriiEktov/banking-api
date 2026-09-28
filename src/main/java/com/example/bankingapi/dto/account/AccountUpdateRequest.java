package com.example.bankingapi.dto.account;

import com.example.bankingapi.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountUpdateRequest {

    @NotNull(message = "Account type should not be null")
    private AccountType accountType;
}

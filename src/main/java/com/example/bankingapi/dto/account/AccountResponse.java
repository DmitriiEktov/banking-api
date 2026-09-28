package com.example.bankingapi.dto.account;

import com.example.bankingapi.enums.AccountType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountResponse {
    private Long accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountHolderResponse holder;
}

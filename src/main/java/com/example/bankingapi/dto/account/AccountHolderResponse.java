package com.example.bankingapi.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountHolderResponse {
    private Long clientId;
    private String fullName;
}

package com.example.bankingapi.dto.account;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountAssignRequest {
    @NotNull(message = "Client id must not be null")
    private Long clientId;
}

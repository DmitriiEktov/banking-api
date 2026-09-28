package com.example.bankingapi.dto.client;

import com.example.bankingapi.dto.account.AccountShortResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClientResponse {

    private Long clientId;
    private String firstName;
    private String lastName;
    private int birthYear;
    private String fullName;
    private List<AccountShortResponse> accounts;
}

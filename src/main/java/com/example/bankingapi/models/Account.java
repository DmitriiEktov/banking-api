package com.example.bankingapi.models;

import com.example.bankingapi.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @NotBlank(message = "Account number should not be blank")
    @Size(min = 1, max = 23, message = "Account number should be between 1 and 23 characters")
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @NotNull(message = "Account type should not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @PositiveOrZero(message = "Balance must be zero or positive")
    @Column(name = "balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public Account(String accountNumber, AccountType accountType, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
    }

    public String getOwnerFullName() {
        return client != null ? client.getFullName() : null;
    }
}

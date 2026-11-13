package com.example.bankingapi.repositories;

import com.example.bankingapi.enums.AccountType;
import com.example.bankingapi.models.Account;
import com.example.bankingapi.models.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    @DisplayName("existsByAccountNumber: should return true when account exists")
    void existsByAccountNumber_shouldReturnTrue() {
        accountRepository.save(new Account("40817810123456789012", AccountType.DEBIT, BigDecimal.ZERO));

        boolean exists = accountRepository.existsByAccountNumber("40817810123456789012");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByAccountNumber: should return false when account does not exist")
    void existsByAccountNumber_shouldReturnFalse() {
        boolean exists = accountRepository.existsByAccountNumber("00000000000000000000");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByClientIsNull: should return only free accounts")
    void findByClientIsNull_shouldReturnOnlyFreeAccounts() {
        accountRepository.save(new Account("11111111111111111111", AccountType.DEBIT, BigDecimal.ZERO));
        Client client = clientRepository.save(new Client("Ivan", "Ivanov", 1990));
        Account takenAccount = new Account("22222222222222222222", AccountType.SAVINGS, BigDecimal.ZERO);
        takenAccount.setClient(client);
        accountRepository.save(takenAccount);

        List<Account> freeAccounts = accountRepository.findByClientIsNull();

        assertThat(freeAccounts).hasSize(1);
        assertThat(freeAccounts.get(0).getAccountNumber()).isEqualTo("11111111111111111111");
    }
}

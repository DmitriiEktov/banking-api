package com.example.bankingapi.services;

import com.example.bankingapi.dto.account.*;
import com.example.bankingapi.enums.AccountType;
import com.example.bankingapi.exceptions.*;
import com.example.bankingapi.mappers.AccountMapper;
import com.example.bankingapi.models.Account;
import com.example.bankingapi.models.Client;
import com.example.bankingapi.repositories.AccountRepository;
import com.example.bankingapi.repositories.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("getAllAccount: should return list of accounts")
    void getAllAccounts_shouldReturnList() {
        Account account1 = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        account1.setAccountId(1L);
        Account account2 = new Account("1b2b3b4b5b6b7b8b9b1b2b3", AccountType.DEBIT, BigDecimal.ZERO);
        account2.setAccountId(2L);

        AccountResponse response1 = new AccountResponse();
        response1.setAccountId(1L);
        response1.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        AccountResponse response2 = new AccountResponse();
        response2.setAccountId(2L);
        response2.setAccountNumber("1b2b3b4b5b6b7b8b9b1b2b3");

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(accountMapper.toResponse(account1)).thenReturn(response1);
        when(accountMapper.toResponse(account2)).thenReturn(response2);

        List<AccountResponse> actual = accountService.getAllAccounts();

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getAccountNumber()).isEqualTo("1a2a3a4a5a6a7a8a9a1a2a3");
        assertThat(actual.get(1).getAccountNumber()).isEqualTo("1b2b3b4b5b6b7b8b9b1b2b3");

        verify(accountRepository, times(1)).findAll();
        verify(accountMapper, times(2)).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("getAllAccounts: should return empty list when no accounts")
    void getAllAccounts_shouldReturnEmptyList() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<AccountResponse> actual = accountService.getAllAccounts();

        assertThat(actual).isEmpty();

        verify(accountMapper, never()).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("getFreeAccounts: should return only free accounts")
    void getFreeAccounts_shouldReturnList() {
        Account freeAccount1 = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        freeAccount1.setAccountId(1L);
        Account freeAccount2 = new Account("1b2b3b4b5b6b7b8b9b1b2b3", AccountType.SAVINGS, BigDecimal.ZERO);
        freeAccount2.setAccountId(2L);

        AccountResponse response1 = new AccountResponse();
        response1.setAccountId(1L);
        AccountResponse response2 = new AccountResponse();
        response2.setAccountId(2L);

        when(accountRepository.findByClientIsNull()).thenReturn(List.of(freeAccount1, freeAccount2));
        when(accountMapper.toResponse(freeAccount1)).thenReturn(response1);
        when(accountMapper.toResponse(freeAccount2)).thenReturn(response2);

        List<AccountResponse> actual = accountService.getFreeAccounts();

        assertThat(actual).hasSize(2);

        verify(accountRepository, times(1)).findByClientIsNull();
        verify(accountMapper, times(2)).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("getFreeAccounts: should return empty list when no free accounts")
    void getFreeAccounts_shouldReturnEmptyList() {
        when(accountRepository.findByClientIsNull()).thenReturn(List.of());

        List<AccountResponse> actual = accountService.getFreeAccounts();

        assertThat(actual).isEmpty();
        verify(accountMapper, never()).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("getAccountById: should return account when found")
    void getAccountById_shouldReturnAccount() {
        Long id = 1L;
        Account account = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        account.setAccountId(id);

        AccountResponse response = new AccountResponse();
        response.setAccountId(id);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(response);

        AccountResponse actual = accountService.getAccountById(id);

        assertThat(actual).isNotNull();
        assertThat(actual.getAccountId()).isEqualTo(id);
        assertThat(actual.getAccountNumber()).isEqualTo("1a2a3a4a5a6a7a8a9a1a2a3");

        verify(accountRepository, times(1)).findById(id);
        verify(accountMapper, times(1)).toResponse(account);
    }

    @Test
    @DisplayName("getAccountById: should throw AccountNotFoundException when not found")
    void getAccountById_NotFound_ThrowsException() {
        Long nonExistentId = 999L;

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(nonExistentId)).isInstanceOf(AccountNotFoundException.class).hasMessageContaining("999");

        verifyNoInteractions(accountMapper);
    }

    @Test
    @DisplayName("createAccount: should create account successfully")
    void createAccount_Success() {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        request.setAccountType(AccountType.DEBIT);
        request.setBalance(BigDecimal.ZERO);

        Account accountToSave = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        Account savedAccount = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        savedAccount.setAccountId(1L);

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setAccountId(1L);
        expectedResponse.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");

        when(accountRepository.existsByAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3")).thenReturn(false);
        when(accountMapper.toEntity(request)).thenReturn(accountToSave);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountMapper.toResponse(savedAccount)).thenReturn(expectedResponse);


        AccountResponse actual = accountService.createAccount(request);

        assertThat(actual).isNotNull();
        assertThat(actual.getAccountId()).isEqualTo(1L);
        assertThat(actual.getAccountNumber()).isEqualTo("1a2a3a4a5a6a7a8a9a1a2a3");

        verify(accountMapper, times(1)).toEntity(request);
        verify(accountRepository, times(1)).save(accountToSave);
        verify(accountMapper, times(1)).toResponse(savedAccount);
    }

    @Test
    @DisplayName("createAccount: should throw AccountNumberDuplicateException when number already exists")
    void createAccount_DuplicateNumber_ThrowsException() {
        String existingNumber = "1a2a3a4a5a6a7a8a9a1a2a3";
        AccountCreateRequest request = new AccountCreateRequest();
        request.setAccountNumber(existingNumber);
        request.setAccountType(AccountType.DEBIT);
        request.setBalance(BigDecimal.ZERO);

        when(accountRepository.existsByAccountNumber(existingNumber)).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(request)).isInstanceOf(AccountNumberDuplicateException.class).hasMessageContaining(existingNumber);

        verify(accountMapper, never()).toEntity(any());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("updateAccount: should update account successfully")
    void updateAccount_Success() {
        Long id = 1L;
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setAccountType(AccountType.CREDIT);

        Account existing = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        existing.setAccountId(id);

        AccountResponse response = new AccountResponse();
        response.setAccountId(id);
        response.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        response.setAccountType(AccountType.CREDIT);
        response.setBalance(BigDecimal.ZERO);

        when(accountRepository.findById(id)).thenReturn(Optional.of(existing));
        when(accountMapper.toResponse(existing)).thenReturn(response);

        AccountResponse actual = accountService.updateAccount(id, request);

        assertThat(actual).isNotNull();
        assertThat(actual.getAccountType()).isEqualTo(AccountType.CREDIT);

        verify(accountRepository, times(1)).findById(id);
        verify(accountMapper, times(1)).updateEntity(existing, request);
        verify(accountMapper, times(1)).toResponse(existing);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("updateAccount: should throw AccountNotFoundException when not found")
    void updateAccount_NotFound_ThrowsException() {
        Long nonExistentId = 999L;
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setAccountType(AccountType.CREDIT);

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccount(nonExistentId, request)).isInstanceOf(AccountNotFoundException.class).hasMessageContaining("999");

        verify(accountMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("deleteAccount: should delete account successfully")
    void deleteAccount_Success() {
        Long id = 1L;

        when(accountRepository.existsById(id)).thenReturn(true);

        accountService.deleteAccount(id);

        verify(accountRepository, times(1)).existsById(id);
        verify(accountRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deleteAccount: should throw AccountNotFoundException when not found")
    void deleteAccount_NotFound_ThrowsException() {
        Long nonExistentId = 999L;

        when(accountRepository.existsById(nonExistentId)).thenReturn(false);

        assertThatThrownBy(() -> accountService.deleteAccount(nonExistentId)).isInstanceOf(AccountNotFoundException.class).hasMessageContaining("999");

        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("assignAccount: should assign account to client successfully")
    void assignAccount_Success() {
        Long accountId = 1L;
        Long clientId = 1L;

        Account existingAccount = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        existingAccount.setAccountId(accountId);

        Client client = new Client("Petr", "Petrov", 1985);
        client.setClientId(clientId);

        AccountAssignRequest request = new AccountAssignRequest();
        request.setClientId(clientId);

        AccountHolderResponse holder = new AccountHolderResponse();
        holder.setClientId(clientId);
        holder.setFullName("Petr Petrov");

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setAccountId(accountId);
        expectedResponse.setAccountNumber("1a2a3a4a5a6a7a8a9a1a2a3");
        expectedResponse.setAccountType(AccountType.DEBIT);
        expectedResponse.setBalance(BigDecimal.ZERO);
        expectedResponse.setHolder(holder);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(accountMapper.toResponse(existingAccount)).thenReturn(expectedResponse);

        AccountResponse actual = accountService.assignAccount(accountId, request);

        assertThat(actual).isNotNull();
        assertThat(actual.getAccountId()).isEqualTo(accountId);
        assertThat(actual.getHolder()).isNotNull();
        assertThat(actual.getHolder().getClientId()).isEqualTo(clientId);
        assertThat(actual.getHolder().getFullName()).isEqualTo("Petr Petrov");
        assertThat(existingAccount.getClient()).isEqualTo(client);

        verify(accountRepository, times(1)).findById(accountId);
        verify(clientRepository, times(1)).findById(clientId);
        verify(accountMapper, times(1)).toResponse(existingAccount);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("assignAccount: should throw AccountAlreadyAssignedException when account is already taken")
    void assignAccount_AlreadyAssigned_ThrowsException() {
        Long accountId = 1L;
        Long existingClientId = 999L;

        Client existingOwner = new Client("Old", "Owner", 1970);
        existingOwner.setClientId(existingClientId);

        Account takenAccount = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        ;
        takenAccount.setAccountId(accountId);
        takenAccount.setClient(existingOwner);

        AccountAssignRequest request = new AccountAssignRequest();
        request.setClientId(1L);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(takenAccount));

        assertThatThrownBy(() -> accountService.assignAccount(accountId, request)).isInstanceOf(AccountAlreadyAssignedException.class).hasMessageContaining("1");

        verify(clientRepository, never()).findById(anyLong());
        verify(accountMapper, never()).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("assignAccount: should throw ClientNotFoundException when client not found")
    void assignAccount_ClientNotFound_ThrowsException() {
        Long accountId = 1L;
        Long nonExistentClientId = 999L;

        Account freeAccount = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        freeAccount.setAccountId(accountId);

        AccountAssignRequest request = new AccountAssignRequest();
        request.setClientId(nonExistentClientId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(freeAccount));
        when(clientRepository.findById(nonExistentClientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.assignAccount(accountId, request)).isInstanceOf(ClientNotFoundException.class).hasMessageContaining("999");

        assertThat(freeAccount.getClient()).isNull();

        verify(accountMapper, never()).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("releaseAccount: should release account successfully")
    void releaseAccount_Success() {
        Long accountId = 1L;
        Client client = new Client("Petr", "Petrov", 1985);
        client.setClientId(accountId);

        Account takenAccount = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        takenAccount.setAccountId(accountId);
        takenAccount.setClient(client);

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setAccountId(accountId);
        expectedResponse.setHolder(null);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(takenAccount));
        when(accountMapper.toResponse(takenAccount)).thenReturn(expectedResponse);

        AccountResponse actual = accountService.releaseAccount(accountId);

        assertThat(actual).isNotNull();
        assertThat(actual.getHolder()).isNull();
        assertThat(takenAccount.getClient()).isNull();

        verify(accountRepository, times(1)).findById(accountId);
        verify(accountMapper, times(1)).toResponse(takenAccount);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("releaseAccount: should throw AccountAlreadyFreeException when account is already free")
    void releaseAccount_AlreadyFree_ThrowsException() {
        Long accountId = 1L;
        Account freeAccount = new Account("1a2a3a4a5a6a7a8a9a1a2a3", AccountType.DEBIT, BigDecimal.ZERO);
        freeAccount.setAccountId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(freeAccount));

        assertThatThrownBy(() -> accountService.releaseAccount(accountId)).isInstanceOf(AccountAlreadyFreeException.class);

        verify(accountMapper, never()).toResponse(any(Account.class));
    }

    @Test
    @DisplayName("releaseAccount: should throw AccountNotFoundException when account not found")
    void releaseAccount_NotFound_ThrowsException() {
        Long nonExistentId = 999L;

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.releaseAccount(nonExistentId)).isInstanceOf(AccountNotFoundException.class);

        verify(accountMapper, never()).toResponse(any(Account.class));
    }
}

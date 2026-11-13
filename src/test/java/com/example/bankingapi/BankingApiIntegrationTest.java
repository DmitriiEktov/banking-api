package com.example.bankingapi;

import com.example.bankingapi.dto.account.AccountAssignRequest;
import com.example.bankingapi.dto.account.AccountCreateRequest;
import com.example.bankingapi.dto.account.AccountResponse;
import com.example.bankingapi.dto.auth.AuthRequest;
import com.example.bankingapi.dto.auth.AuthResponse;
import com.example.bankingapi.dto.auth.RegisterRequest;
import com.example.bankingapi.dto.client.ClientCreateRequest;
import com.example.bankingapi.dto.client.ClientResponse;
import com.example.bankingapi.dto.client.ClientUpdateRequest;
import com.example.bankingapi.enums.AccountType;
import com.example.bankingapi.enums.Role;
import com.example.bankingapi.models.User;
import com.example.bankingapi.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BankingApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByUsername("testuser")) {
            userRepository.save(new User("testuser", passwordEncoder.encode("test123"), Role.USER));
        }
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
        }
    }

    @Test
    @DisplayName("fullClientLifecycle: login, create client, get client")
    void fullClientLifecycle() {
        HttpHeaders headers = getAuthHeaders("testuser", "test123");

        ClientCreateRequest createRequest = new ClientCreateRequest();
        createRequest.setFirstName("Ivan");
        createRequest.setLastName("Ivanov");
        createRequest.setBirthYear(1990);

        ResponseEntity<ClientResponse> createResponse = restTemplate.exchange(
                "/api/clients", HttpMethod.POST, new HttpEntity<>(createRequest, headers), ClientResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getFullName()).isEqualTo("Ivan Ivanov");

        Long clientId = createResponse.getBody().getClientId();

        ResponseEntity<ClientResponse> getResponse = restTemplate.exchange(
                "/api/clients/" + clientId, HttpMethod.GET, new HttpEntity<>(headers), ClientResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getClientId()).isEqualTo(clientId);
    }

    @Test
    @DisplayName("GET /api/clients: without token should return 401")
    void getClientsWithoutToken_shouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/clients", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("DELETE /api/clients/{id}: as USER should return 403")
    void deleteClientAsUser_shouldReturnForbidden() {
        HttpHeaders headers = getAuthHeaders("testuser", "test123");

        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/clients/1", HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("authFlow: register new user and login with bad credentials")
    void authFlow_registerAndBadLogin() {
        String uniqueUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(uniqueUsername);
        registerRequest.setPassword("securePassword123");

        ResponseEntity<AuthResponse> regResponse = restTemplate.postForEntity(
                "/api/auth/register", registerRequest, AuthResponse.class);

        assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(regResponse.getBody().getToken()).isNotBlank();

        AuthRequest badRequest = new AuthRequest();
        badRequest.setUsername(uniqueUsername);
        badRequest.setPassword("wrongPassword");

        ResponseEntity<String> badLoginResponse = restTemplate.postForEntity(
                "/api/auth/login", badRequest, String.class);

        assertThat(badLoginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("accountFlow: create, assign to client, and release")
    void accountFlow_createAssignAndRelease() {
        HttpHeaders headers = getAuthHeaders("testuser", "test123");

        Long clientId = createTestClient("Account", "Tester", 1995, headers);

        String accountNumber = "ACC" + UUID.randomUUID().toString().substring(0, 10);
        Long accountId = createTestAccount(accountNumber, AccountType.DEBIT, headers);

        AccountAssignRequest assignReq = new AccountAssignRequest();
        assignReq.setClientId(clientId);
        ResponseEntity<AccountResponse> assignRes = restTemplate.exchange(
                "/api/accounts/" + accountId + "/assign", HttpMethod.POST, new HttpEntity<>(assignReq, headers), AccountResponse.class);

        assertThat(assignRes.getBody().getHolder().getClientId()).isEqualTo(clientId);

        ResponseEntity<AccountResponse> releaseRes = restTemplate.exchange(
                "/api/accounts/" + accountId + "/release", HttpMethod.POST, new HttpEntity<>(headers), AccountResponse.class);

        assertThat(releaseRes.getBody().getHolder()).isNull();
    }

    @Test
    @DisplayName("accountFlow: get free accounts")
    void accountFlow_getFreeAccounts() {
        HttpHeaders headers = getAuthHeaders("testuser", "test123");

        ResponseEntity<AccountResponse[]> response = restTemplate.exchange(
                "/api/accounts/free", HttpMethod.GET, new HttpEntity<>(headers), AccountResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("clientFlow: update client")
    void clientFlow_updateClient() {
        HttpHeaders headers = getAuthHeaders("testuser", "test123");

        Long clientId = createTestClient("OldName", "OldLast", 1980, headers);

        ClientUpdateRequest updateReq = new ClientUpdateRequest();
        updateReq.setFirstName("NewName");
        updateReq.setLastName("NewLast");
        updateReq.setBirthYear(1985);

        ResponseEntity<ClientResponse> response = restTemplate.exchange(
                "/api/clients/" + clientId, HttpMethod.PUT, new HttpEntity<>(updateReq, headers), ClientResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFullName()).isEqualTo("NewName NewLast");
    }

    @Test
    @DisplayName("DELETE /api/clients/{id}: deleting client should free their accounts in DB")
    void deleteClient_shouldSetAccountsToNull() {
        HttpHeaders headers = getAuthHeaders("admin", "admin123");

        Long clientId = createTestClient("Cascade", "Delete", 1990, headers);

        String accountNumber = "ACC" + UUID.randomUUID().toString().substring(0, 10);
        Long accountId = createTestAccount(accountNumber, AccountType.DEBIT, headers);

        AccountAssignRequest assignReq = new AccountAssignRequest();
        assignReq.setClientId(clientId);
        restTemplate.exchange("/api/accounts/" + accountId + "/assign", HttpMethod.POST, new HttpEntity<>(assignReq, headers), AccountResponse.class);

        restTemplate.exchange("/api/clients/" + clientId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

        ResponseEntity<AccountResponse> accountCheck = restTemplate.exchange(
                "/api/accounts/" + accountId, HttpMethod.GET, new HttpEntity<>(headers), AccountResponse.class);

        assertThat(accountCheck.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accountCheck.getBody()).isNotNull();
        assertThat(accountCheck.getBody().getHolder()).isNull();
    }

    @Test
    @DisplayName("clientFlow: get non-existent client should return 404")
    void clientFlow_getNotFound() {
        HttpHeaders headers = getAuthHeaders("testuser", "test123");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/clients/999999", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private HttpHeaders getAuthHeaders(String username, String password) {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/login", authRequest, AuthResponse.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(response.getBody().getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Long createTestClient(String firstName, String lastName, int birthYear, HttpHeaders headers) {
        ClientCreateRequest req = new ClientCreateRequest();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setBirthYear(birthYear);

        ResponseEntity<ClientResponse> response = restTemplate.exchange(
                "/api/clients", HttpMethod.POST, new HttpEntity<>(req, headers), ClientResponse.class);
        return response.getBody().getClientId();
    }

    private Long createTestAccount(String accountNumber, AccountType type, HttpHeaders headers) {
        AccountCreateRequest req = new AccountCreateRequest();
        req.setAccountNumber(accountNumber);
        req.setAccountType(type);
        req.setBalance(BigDecimal.ZERO);

        ResponseEntity<AccountResponse> response = restTemplate.exchange(
                "/api/accounts", HttpMethod.POST, new HttpEntity<>(req, headers), AccountResponse.class);
        return response.getBody().getAccountId();
    }
}
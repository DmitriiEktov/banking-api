package com.example.bankingapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username should not be blank")
    @Size(min = 3, max = 100, message = "Username should be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password should not be blank")
    @Size(min = 6, max = 100, message = "Password should be between 6 and 100 characters")
    private String password;
}

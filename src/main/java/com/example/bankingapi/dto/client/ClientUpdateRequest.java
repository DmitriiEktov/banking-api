package com.example.bankingapi.dto.client;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientUpdateRequest {

    @NotBlank(message = "First name should not be blank")
    @Size(min = 1, max = 255, message = "First name should be between 1 and 255 characters")
    private String firstName;

    @NotBlank(message = "Last name should not be blank")
    @Size(min = 1, max = 255, message = "Last name should be between 1 and 255 characters")
    private String lastName;

    @Min(value = 1900, message = "Birth year cannot be earlier than 1900")
    @Max(value = 2026, message = "Birth year cannot be later than the current year")
    private int birthYear;
}

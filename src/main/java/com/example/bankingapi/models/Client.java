package com.example.bankingapi.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long clientId;

    @NotBlank(message = "First name should not be blank")
    @Size(min = 1, max = 255, message = "First name should be between 1 and 255 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name should not be blank")
    @Size(min = 1, max = 255, message = "Last name should be between 1 and 255 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Min(value = 1900, message = "Birth year cannot be earlier than 1900")
    @Max(value = 2026, message = "Birth year cannot be later than the current year")
    @Column(name = "birth_year", nullable = false)
    private int birthYear;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public Client(String firstName, String lastName, int birthYear) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

package com.andrija.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO za login.
 * Sadrži samo email i lozinku — sistem ga autentifikuje i vraća JWT token.
 */
public record LoginRequest(

        @Email(message = "Email format nije ispravan")
        @NotBlank(message = "Email je obavezan")
        String email,

        @NotBlank(message = "Lozinka je obavezna")
        String password
) {}

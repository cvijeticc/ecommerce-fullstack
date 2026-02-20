package com.andrija.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) za registraciju korisnika.
 *
 * Koristimo Java record (Java 16+) — immutable objekat, kraći kod.
 * Record automatski generiše: konstruktor, getere, equals, hashCode, toString.
 *
 * Bean Validation anotacije (@NotBlank, @Email, @Size) se aktiviraju
 * u controlleru sa @Valid anotacijom.
 * Ako validacija ne prođe, Spring automatski baca MethodArgumentNotValidException
 * koju hvatamo u GlobalExceptionHandler-u.
 */
public record RegisterRequest(

        @NotBlank(message = "Ime je obavezno")
        String firstName,

        @NotBlank(message = "Prezime je obavezno")
        String lastName,

        @Email(message = "Email format nije ispravan")
        @NotBlank(message = "Email je obavezan")
        String email,

        @Size(min = 6, message = "Lozinka mora imati minimum 6 karaktera")
        @NotBlank(message = "Lozinka je obavezna")
        String password
) {}

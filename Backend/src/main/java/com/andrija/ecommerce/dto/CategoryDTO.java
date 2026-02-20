package com.andrija.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO za kategoriju.
 * Jednostavan — samo id, naziv i opis.
 */
public record CategoryDTO(
        Long id,

        @NotBlank(message = "Naziv kategorije je obavezan")
        String name,

        String description
) {}

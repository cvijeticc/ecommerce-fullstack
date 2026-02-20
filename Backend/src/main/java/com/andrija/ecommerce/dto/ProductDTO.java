package com.andrija.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO za proizvod — koristi se i za REQUEST (kreiranje/izmena) i za RESPONSE (prikaz).
 *
 * Zašto ne vraćamo direktno Product entitet?
 * 1. Entitet ima @ManyToOne Category sa Lazy loading — može izazvati LazyInitializationException
 * 2. Entitet može imati povratne veze (products lista u Category) — Jackson pravi infinite loop
 * 3. Ne želimo da izložimo interne detalje (npr. createdAt, ceo category objekat)
 *
 * categoryName — vraćamo samo naziv kategorije (String), ne ceo Category objekat.
 * categoryId   — potreban pri kreiranju/izmeni da znamo kojoj kategoriji pripada.
 */
public record ProductDTO(
        Long id,                    // null pri kreiranju, popunjava ga baza

        @NotBlank(message = "Naziv proizvoda je obavezan")
        String name,

        String description,

        @NotNull(message = "Cena je obavezna")
        @DecimalMin(value = "0.01", message = "Cena mora biti veća od 0")
        BigDecimal price,

        @NotNull(message = "Količina na stanju je obavezna")
        @Min(value = 0, message = "Količina ne može biti negativna")
        Integer stockQuantity,

        String imageUrl,

        String categoryName,        // za prikaz (response)
        Long categoryId             // za kreiranje/izmenu (request)
) {}

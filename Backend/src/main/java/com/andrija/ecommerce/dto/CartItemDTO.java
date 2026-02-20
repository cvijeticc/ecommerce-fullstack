package com.andrija.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO za stavku korpe.
 *
 * Kada klijent DODAJE u korpu: šalje productId i quantity.
 * Kada server VRAĆA korpu: šalje id, productId, productName, price, quantity.
 *
 * subtotal = price * quantity — računamo u servisu za prikaz.
 */
public record CartItemDTO(
        Long id,                    // ID CartItem-a (za update/delete)

        @NotNull(message = "ID proizvoda je obavezan")
        Long productId,

        String productName,         // za prikaz na frontendu
        String imageUrl,
        BigDecimal price,           // trenutna cena (za prikaz u korpi)

        @NotNull(message = "Količina je obavezna")
        @Min(value = 1, message = "Količina mora biti minimum 1")
        Integer quantity,

        BigDecimal subtotal         // price * quantity — computed field
) {}

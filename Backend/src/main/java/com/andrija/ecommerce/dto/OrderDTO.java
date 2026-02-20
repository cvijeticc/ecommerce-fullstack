package com.andrija.ecommerce.dto;

import com.andrija.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO za porudžbinu.
 *
 * Koristi se i kao:
 * - REQUEST: kada korisnik kreira porudžbinu šalje samo shippingAddress
 * - RESPONSE: server vraća sve detalje (id, status, stavke...)
 *
 * Uočavamo da ne vraćamo ceo User objekat — samo userEmail,
 * da ne bi eksponirali osetljive podatke (password hash itd).
 */
public record OrderDTO(
        Long id,

        String userEmail,           // ko je naručio (samo za admin pregled)
        BigDecimal totalAmount,
        OrderStatus status,

        @NotBlank(message = "Adresa dostave je obavezna")
        String shippingAddress,

        List<OrderItemDTO> items,
        LocalDateTime createdAt
) {}

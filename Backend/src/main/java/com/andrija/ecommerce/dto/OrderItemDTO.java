package com.andrija.ecommerce.dto;

import java.math.BigDecimal;

/**
 * DTO za stavku porudžbine — deo OrderDTO-a.
 * Prikazuje šta je naručeno, koliko i po kojoj ceni.
 *
 * priceAtPurchase — istorijska cena, ne sadašnja cena proizvoda!
 */
public record OrderItemDTO(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtPurchase,
        BigDecimal subtotal         // priceAtPurchase * quantity
) {}

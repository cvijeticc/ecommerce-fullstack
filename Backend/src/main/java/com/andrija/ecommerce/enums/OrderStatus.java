package com.andrija.ecommerce.enums;

/**
 * Enum koji prati životni ciklus porudžbine.
 *
 * PENDING   — porudžbina kreirana, čeka obradu
 * CONFIRMED — prodavac potvrdio porudžbinu
 * SHIPPED   — porudžbina poslata na adresu
 * DELIVERED — kupac primio porudžbinu
 * CANCELLED — porudžbina otkazana (može se otkazati dok nije SHIPPED)
 *
 * Samo ADMIN može da menja status putem PUT /api/admin/orders/{id}/status
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

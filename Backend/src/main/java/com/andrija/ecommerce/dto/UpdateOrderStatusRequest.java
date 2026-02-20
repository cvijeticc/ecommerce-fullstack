package com.andrija.ecommerce.dto;

import com.andrija.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO za izmenu statusa porudžbine.
 * Koristi ga ADMIN putem PUT /api/admin/orders/{id}/status.
 *
 * Primer request body: { "status": "SHIPPED" }
 */
public record UpdateOrderStatusRequest(
        @NotNull(message = "Status je obavezan")
        OrderStatus status
) {}

package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.dto.OrderDTO;
import com.andrija.ecommerce.dto.UpdateOrderStatusRequest;
import com.andrija.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler za porudžbine.
 *
 * Dva skupa endpointova:
 * 1. /api/orders   — za ulogovanog CUSTOMER-a (moje porudžbine)
 * 2. /api/admin/orders — za ADMIN-a (sve porudžbine svih korisnika)
 *
 * Zaštita je konfigurisana u SecurityConfig:
 * - /api/admin/**  → hasRole("ADMIN")
 * - sve ostalo     → authenticated()
 */
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ─── Customer endpointovi ─────────────────────────────────────────────────

    /**
     * POST /api/orders
     * Kreira novu porudžbinu iz sadržaja korpe.
     *
     * Request body: { "shippingAddress": "Bulevar oslobođenja 1, Novi Sad" }
     *
     * @return 201 Created sa detaljima porudžbine
     */
    @PostMapping("/api/orders")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request.shippingAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * GET /api/orders
     * Vraća sve moje porudžbine (sortirane od najnovije).
     */
    @GetMapping("/api/orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    /**
     * GET /api/orders/{id}
     * Vraća detalje jedne porudžbine (mora biti moja!).
     * Ako pokušam da pristupim tuđoj — 404 Not Found (sigurnosna provera u servisu).
     */
    @GetMapping("/api/orders/{id}")
    public ResponseEntity<OrderDTO> getMyOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrderById(id));
    }

    // ─── Admin endpointovi ────────────────────────────────────────────────────

    /**
     * GET /api/admin/orders
     * Admin: vraća SVE porudžbine od SVIH korisnika.
     */
    @GetMapping("/api/admin/orders")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * PUT /api/admin/orders/{id}/status
     * Admin: menja status porudžbine.
     *
     * Request body: { "status": "SHIPPED" }
     * Moguće vrednosti: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
     */
    @PutMapping("/api/admin/orders/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderDTO updated = orderService.updateOrderStatus(id, request.status());
        return ResponseEntity.ok(updated);
    }

    // ─── Interni Request DTO ──────────────────────────────────────────────────

    record CreateOrderRequest(
            @NotBlank(message = "Adresa dostave je obavezna") String shippingAddress
    ) {}
}

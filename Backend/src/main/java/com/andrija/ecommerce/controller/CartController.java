package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.dto.CartItemDTO;
import com.andrija.ecommerce.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler za upravljanje korpom.
 *
 * Svi endpointovi zahtevaju autentifikaciju!
 * Konfigurisano u SecurityConfig: .anyRequest().authenticated()
 *
 * Korisnik se NIKAD ne identifikuje iz URL-a ili body-ja,
 * već UVEK iz JWT tokena koji servis čita iz SecurityContext-a.
 *
 * Primer: GET /api/cart NE prima userId — CartService sam zna ko je ulogovan.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart
     * Vraća sve stavke korpe trenutnog korisnika.
     */
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    /**
     * POST /api/cart
     * Dodaje proizvod u korpu ili povećava količinu ako već postoji.
     *
     * Request body: { "productId": 5, "quantity": 2 }
     *
     * Koristimo interni record kao request DTO — kraće nego posebna klasa.
     */
    @PostMapping
    public ResponseEntity<CartItemDTO> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartItemDTO item = cartService.addToCart(request.productId(), request.quantity());
        return ResponseEntity.ok(item);
    }

    /**
     * PUT /api/cart/{cartItemId}
     * Ažurira količinu stavke u korpi.
     *
     * Request body: { "quantity": 3 }
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateQuantityRequest request
    ) {
        CartItemDTO updated = cartService.updateCartItem(cartItemId, request.quantity());
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/cart/{cartItemId}
     * Uklanja jednu stavku iz korpe.
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/cart
     * Briše celu korpu korisnika.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    // ─── Interni Request DTO-ovi ──────────────────────────────────────────────
    // Definisani kao inner records — ne trebaju posebne fajlove jer su jednostavni

    record AddToCartRequest(
            @NotNull(message = "ID proizvoda je obavezan") Long productId,
            @NotNull @Min(value = 1, message = "Količina mora biti minimum 1") Integer quantity
    ) {}

    record UpdateQuantityRequest(
            @NotNull @Min(value = 1, message = "Količina mora biti minimum 1") Integer quantity
    ) {}
}

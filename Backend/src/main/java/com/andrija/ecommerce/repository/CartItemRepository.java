package com.andrija.ecommerce.repository;

import com.andrija.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repozitorijum za stavke korpe.
 *
 * Sve metode uzimaju userId — svaki korisnik vidi samo svoju korpu.
 * UserId se nikad ne uzima iz request body-ja, uvek iz JWT tokena!
 *
 * deleteByUserId — briše celu korpu odjednom (poziva se posle uspešne porudžbine).
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Učitava sve stavke korpe za određenog korisnika
    List<CartItem> findByUserId(Long userId);

    // Vraća stavku samo ako pripada tom korisniku (sigurnosna provera)
    Optional<CartItem> findByIdAndUserId(Long cartItemId, Long userId);

    // Briše celu korpu korisnika — poziva se posle kreiranja porudžbine
    void deleteByUserId(Long userId);

    // Proverava da li određeni proizvod već postoji u korpi
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
}

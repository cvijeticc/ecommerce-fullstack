package com.andrija.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entitet koji predstavlja jednu stavku u korpi korisnika.
 *
 * Korpa nije "objekat" — sastoji se od CartItem redova u bazi.
 * Svaki red = jedan proizvod u korpi jednog korisnika.
 *
 * Životni ciklus: CartItem se kreira kad korisnik doda u korpu,
 * a briše se kada:
 *   1) Korisnik ručno ukloni stavku DELETE /api/cart/{id}
 *   2) Korisnik napravi porudžbinu — tada se brišu SVE stavke korpe
 */
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Čiji je ovo cart item.
     * Korisnik se uvek identifikuje iz JWT tokena — nikad iz request body-ja!
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Koji proizvod je u korpi
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}

package com.andrija.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entitet koji predstavlja jednu stavku porudžbine.
 * Jedna porudžbina može imati više stavki (npr. 2x Telefon, 1x Slušalice).
 *
 * Ključno polje: priceAtPurchase — cena u trenutku kupovine.
 * Razlog: ako prodavac promeni cenu proizvoda posle porudžbine,
 * stare porudžbine moraju i dalje prikazivati originalnu cenu.
 * Nikada ne koristimo product.getPrice() za prikaz istorijskih porudžbina!
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Veza sa porudžbinom kojoj ova stavka pripada.
     * Cascade se ne postavlja ovde — njime upravlja Order entitet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Koji proizvod je poručen
    @ManyToOne(fetch = FetchType.EAGER) // EAGER jer uvek prikazujemo naziv/cenu
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // Snapshot cene u trenutku kupovine — NE menjati naknadno!
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
}

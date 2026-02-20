package com.andrija.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entitet koji predstavlja proizvod u prodavnici.
 *
 * BigDecimal se koristi za cene jer double/float imaju greške zaokruživanja
 * (npr. 0.1 + 0.2 != 0.3 u floating point aritmetici).
 *
 * stockQuantity prati raspoloživ broj komada — smanjuje se pri porudžbini.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // columnDefinition = "TEXT" — bez ograničenja dužine (za duže opise)
    @Column(columnDefinition = "TEXT")
    private String description;

    // precision = 10, scale = 2 → max vrednost: 99999999.99
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Broj komada na stanju — proveravamo pre svake porudžbine
    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 255)
    private String imageUrl;

    /**
     * Veza sa kategorijom: mnogo proizvoda pripada jednoj kategoriji (ManyToOne).
     * @JoinColumn(name = "category_id") — ime FK kolone u tabeli products.
     * fetch = LAZY — kategorija se učitava samo kad se eksplicitno pristupa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

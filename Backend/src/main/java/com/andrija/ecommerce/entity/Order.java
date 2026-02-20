package com.andrija.ecommerce.entity;

import com.andrija.ecommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entitet koji predstavlja porudžbinu.
 *
 * @Table(name = "orders") — jer "order" je rezervisana SQL reč.
 *
 * Najvažnija veza: Order → OrderItem sa cascade = ALL.
 * To znači: kada sačuvamo Order, Hibernate automatski čuva i sve OrderItem-ove.
 * Kada obrišemo Order, automatski se brišu i svi OrderItem-ovi.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Veza sa korisnikom koji je napravio porudžbinu.
     * nullable = false — porudžbina mora imati korisnika.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ukupan iznos porudžbine (zbir svih stavki)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Status porudžbine — menja ga ADMIN
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 255)
    private String shippingAddress;

    /**
     * cascade = ALL — sve operacije (save, delete...) se propagiraju na stavke.
     * orphanRemoval = true — ako uklonimo stavku iz liste, automatski se briše iz baze.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

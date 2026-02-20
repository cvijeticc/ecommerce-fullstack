package com.andrija.ecommerce.repository;

import com.andrija.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repozitorijum za porudžbine.
 *
 * findByUserId — vraća sve porudžbine jednog korisnika (GET /api/orders).
 * Sortiranje po createdAt DESC (najnovije prve) radimo u servisu sa Sort objekat.
 *
 * findByIdAndUserId — kritična sigurnosna metoda!
 * Korisnik ne sme da vidi tuđu porudžbinu.
 * Čak i ako zna ID, upit neće pronaći porudžbinu ako userId ne odgovara.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Za GET /api/orders — moje porudžbine
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Za GET /api/orders/{id} — jedna moja porudžbina (+ sigurnosna provera)
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}

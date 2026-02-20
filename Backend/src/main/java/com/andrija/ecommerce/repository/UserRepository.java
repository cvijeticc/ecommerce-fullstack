package com.andrija.ecommerce.repository;

import com.andrija.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitorijum za korisnika.
 *
 * JpaRepository<User, Long> nam daje "besplatno":
 *   save(), findById(), findAll(), deleteById(), count()... i još mnogo toga.
 *
 * findByEmail — Spring Data JPA automatski generiše SQL:
 *   SELECT * FROM users WHERE email = ?
 * Samo po konvenciji imenovanja metode!
 *
 * Optional<User> — vraćamo Optional jer email možda ne postoji u bazi.
 * Pozivamo .orElseThrow() kada hoćemo da bacimo izuzetak ako ne postoji.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Koristi se pri login-u i u CustomUserDetailsService
    Optional<User> findByEmail(String email);

    // Koristi se pri registraciji — proverava da li email već postoji
    boolean existsByEmail(String email);
}

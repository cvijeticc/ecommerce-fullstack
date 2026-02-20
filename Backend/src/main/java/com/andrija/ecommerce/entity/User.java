package com.andrija.ecommerce.entity;

import com.andrija.ecommerce.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entitet koji predstavlja korisnika sistema.
 *
 * Implementira UserDetails — Spring Security interfejs koji opisuje korisnika.
 * Zahvaljujući tome, Spring Security može direktno da koristi ovaj objekat
 * za autentifikaciju i autorizaciju (ne treba nam posebna klasa).
 *
 * @Table(name = "users") — jer "user" je rezervisana reč u PostgreSQL-u.
 */
@Entity
@Table(name = "users")
@Data                   // Lombok: generiše getere, setere, toString, equals, hashCode
@NoArgsConstructor      // Lombok: prazan konstruktor (JPA obavezno zahteva)
@AllArgsConstructor     // Lombok: konstruktor sa svim poljima
@Builder                // Lombok: Builder pattern — User.builder().email("...").build()
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT u bazi
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    // Email je jedinstveni identifikator — koristi se za login
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // Lozinka se čuva kao BCrypt hash — nikada plain text!
    @Column(nullable = false, length = 255)
    private String password;

    // Role se čuva kao String u bazi: "ADMIN" ili "CUSTOMER"
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // Automatski se postavlja na trenutno vreme pri INSERT-u
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // mappedBy = "user" znači da Order entitet drži FK (user_id)
    // fetch = LAZY — porudžbine se učitavaju tek kad se eksplicitno zatraže
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders;

    // ─── UserDetails metode (Spring Security interfejs) ──────────────────────

    /**
     * Vraća prava (roles/permissions) korisnika.
     * Format je "ROLE_ADMIN" ili "ROLE_CUSTOMER" — Spring Security zahteva prefiks "ROLE_".
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Spring Security koristi email kao username (ne postoji posebno "username" polje).
     */
    @Override
    public String getUsername() {
        return email;
    }

    // Sledeće metode vraćaju true — nema logike za lock/expire naloga
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}

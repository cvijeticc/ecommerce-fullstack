package com.andrija.ecommerce.repository;

import com.andrija.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repozitorijum za proizvode.
 *
 * Podržava paginaciju (Pageable) — frontend šalje ?page=0&size=10
 * i dobija samo prvu stranicu sa 10 proizvoda, ne sve iz baze.
 *
 * @Query — JPQL upit (Java Persistence Query Language).
 * Razlika od SQL: radimo sa Java klasama (Product, Category), ne tabelama.
 * LOWER() — case-insensitive pretraga (da radi i "telefon" i "Telefon").
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Svi proizvodi po kategoriji (za filtriranje na frontendu)
    List<Product> findByCategoryId(Long categoryId);

    // Provera postoje li proizvodi u kategoriji (za validaciju pre brisanja)
    boolean existsByCategoryId(Long categoryId);

    // Pretraga po imenu — paginovano, case-insensitive
    // %:name% znači "sadrži taj string" (LIKE %...%)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> searchByName(@Param("name") String name, Pageable pageable);

    // Svi proizvodi po kategoriji — paginovano
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
}

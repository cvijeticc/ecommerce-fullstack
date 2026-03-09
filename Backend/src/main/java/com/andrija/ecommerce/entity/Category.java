package com.andrija.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entitet koji predstavlja kategoriju proizvoda (npr. Elektronika, Odeća...).
 *
 * Veza sa Product: jedan Category ima mnogo Product-a (OneToMany).
 * FK (category_id) se nalazi u tabeli products — "deca" uvek drže strani ključ.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Naziv kategorije mora biti jedinstven — ne može biti dve "Elektronike"
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    /**
     * mappedBy = "category" — Product entitet ima @ManyToOne polje "category"
     * koje drži FK. Ovde samo "gledamo" tu vezu sa druge strane.
     *
     * cascade = ALL ovde NIJE postavljen — brisanje kategorije NE briše automatski
     * sve proizvode (to bi bio problem u produkciji).
     *
     * @JsonIgnore — sprečava LazyInitializationException.
     * Bez ovoga, Jackson bi pokušao da serijalizuje listu proizvoda,
     * ali Hibernate sesija je već zatvorena pa baca grešku.
     * Kategorija ne treba da vraća svoje proizvode u odgovoru.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}

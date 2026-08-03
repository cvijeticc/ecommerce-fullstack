package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.dto.CategoryDTO;
import com.andrija.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler za upravljanje kategorijama.
 *
 * GET endpointovi — javni (konfigurisano u SecurityConfig):
 * .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
 *
 * POST/PUT/DELETE endpointovi — samo ADMIN, kroz @PreAuthorize na svakoj metodi.
 * Pravilo .requestMatchers("/api/admin/**").hasRole("ADMIN") iz SecurityConfig-a
 * NE pokriva ove putanje (one su /api/categories/**), pa bi bez @PreAuthorize
 * svaki ulogovan kupac mogao da menja i briše kategorije.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Javni endpoint — prikaz svih kategorija (za filtriranje na frontendu).
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * POST /api/categories
     * Kreira novu kategoriju — samo ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO request) {
        CategoryDTO created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/categories/{id}
     * Ažurira kategoriju — samo ADMIN.
     *
     * @PathVariable — čita {id} iz URL putanje
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO request
    ) {
        CategoryDTO updated = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/categories/{id}
     * Briše kategoriju — samo ADMIN.
     *
     * @return 204 No Content — uspešno, ali nema tela odgovora
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

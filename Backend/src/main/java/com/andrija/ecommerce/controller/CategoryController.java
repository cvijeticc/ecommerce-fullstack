package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.dto.CategoryDTO;
import com.andrija.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler za upravljanje kategorijama.
 *
 * GET endpointovi — javni (konfigurisano u SecurityConfig):
 * .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
 *
 * POST/PUT/DELETE endpointovi — samo ADMIN:
 * .requestMatchers("/api/admin/**").hasRole("ADMIN")
 * NAPOMENA: Admin CRUD za kategorije je ovde na /api/categories,
 * ali zaštita je kroz hasRole na nivou samog kontrolera
 * (alternativno: možemo koristiti @PreAuthorize anotaciju).
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

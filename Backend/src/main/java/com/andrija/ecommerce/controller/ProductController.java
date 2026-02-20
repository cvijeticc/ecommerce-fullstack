package com.andrija.ecommerce.controller;

import com.andrija.ecommerce.dto.ProductDTO;
import com.andrija.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler za upravljanje proizvodima.
 *
 * Javni GET endpointovi — svi mogu pregledati proizvode.
 * Admin POST/PUT/DELETE endpointovi — SecurityConfig ih štiti.
 *
 * Paginacija: Spring Data Web automatski parsira ?page=0&size=10&sort=name,asc
 * iz URL parametara u Pageable objekat.
 * @PageableDefault — podrazumevane vrednosti ako frontend ne šalje parametre.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products?search=telefon&page=0&size=10&sort=price,asc
     *
     * Vraća paginovanu listu proizvoda.
     * Opcioni search parametar filtrira po imenu (case-insensitive).
     *
     * Page<ProductDTO> sadrži:
     * - content: lista DTO-ova za tu stranicu
     * - totalElements: ukupan broj proizvoda
     * - totalPages: ukupan broj stranica
     * - number: trenutna stranica
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getAllProducts(search, pageable));
    }

    /**
     * GET /api/products/{id}
     * Vraća jedan proizvod po ID-ju — 404 ako ne postoji.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * GET /api/products/category/{categoryId}
     * Vraća sve proizvode iz određene kategorije.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * POST /api/products
     * Kreira novi proizvod — samo ADMIN.
     * SecurityConfig: admin upiti zahtevaju "ROLE_ADMIN".
     *
     * @return 201 Created sa kreiranim proizvodom
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO request) {
        ProductDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/products/{id}
     * Ažurira postojeći proizvod — samo ADMIN.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * DELETE /api/products/{id}
     * Briše proizvod — samo ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

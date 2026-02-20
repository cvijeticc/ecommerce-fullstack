package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.ProductDTO;
import com.andrija.ecommerce.entity.Category;
import com.andrija.ecommerce.entity.Product;
import com.andrija.ecommerce.exception.ResourceNotFoundException;
import com.andrija.ecommerce.repository.CategoryRepository;
import com.andrija.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za upravljanje proizvodima.
 *
 * Javne operacije (READ): svi korisnici
 * Admin operacije (CREATE/UPDATE/DELETE): samo ADMIN
 *
 * Podržava paginaciju za listu proizvoda — frontend šalje page i size parametre.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Vraća sve proizvode sa paginacijom i opcionalnom pretragom po imenu.
     *
     * Paginacija sprečava učitavanje hiljada proizvoda odjednom.
     * Pageable = informacija o stranici (broj, veličina, sortiranje).
     *
     * @param search opcioni search string (null = svi proizvodi)
     * @param pageable paginacija i sortiranje
     * @return Page<ProductDTO> — lista za tu stranicu + meta info (ukupno, stranice...)
     */
    public Page<ProductDTO> getAllProducts(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            // Pretraga po imenu — case-insensitive, partial match
            return productRepository.searchByName(search, pageable).map(this::toDTO);
        }
        // Svi proizvodi
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    /**
     * Vraća jedan proizvod po ID-ju.
     * Baca 404 ako ne postoji.
     */
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Proizvod nije pronađen sa id: " + id
                ));
        return toDTO(product);
    }

    /**
     * Vraća sve proizvode iz određene kategorije.
     */
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        // Proveravamo da li kategorija postoji
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Kategorija nije pronađena sa id: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Kreira novi proizvod.
     * Samo ADMIN (zaštićeno u SecurityConfig-u).
     */
    public ProductDTO createProduct(ProductDTO request) {
        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategorija nije pronađena sa id: " + request.categoryId()
                    ));
        }

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .imageUrl(request.imageUrl())
                .category(category)
                .build();

        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    /**
     * Ažurira postojeći proizvod.
     * Samo ADMIN.
     */
    public ProductDTO updateProduct(Long id, ProductDTO request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Proizvod nije pronađen sa id: " + id
                ));

        // Ažuriramo sva polja
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());

        // Ažuriramo kategoriju ako je prosleđena
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Kategorija nije pronađena sa id: " + request.categoryId()
                    ));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    /**
     * Briše proizvod.
     * Samo ADMIN.
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proizvod nije pronađen sa id: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Konvertuje Product entitet u ProductDTO.
     *
     * Važno: category može biti null (ako proizvod nema kategoriju).
     * Koristimo ternarni operator da izbegnemo NullPointerException.
     */
    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getId() : null
        );
    }
}

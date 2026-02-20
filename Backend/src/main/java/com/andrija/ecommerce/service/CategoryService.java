package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.CategoryDTO;
import com.andrija.ecommerce.entity.Category;
import com.andrija.ecommerce.exception.DuplicateEmailException;
import com.andrija.ecommerce.exception.ResourceNotFoundException;
import com.andrija.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servis za upravljanje kategorijama.
 *
 * CRUD operacije:
 * - READ: svi mogu da vide kategorije (javni endpoint)
 * - CREATE/UPDATE/DELETE: samo ADMIN (zaštićeno SecurityConfig-om)
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Vraća sve kategorije — koristi se za prikaz filtera na frontendu.
     */
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)  // konvertujemo svaki entitet u DTO
                .toList();
    }

    /**
     * Kreira novu kategoriju.
     * Baca grešku ako kategorija sa tim imenom već postoji.
     */
    public CategoryDTO createCategory(CategoryDTO request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateEmailException( // koristimo isti 409 exception tip
                "Kategorija sa nazivom '" + request.name() + "' već postoji"
            );
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    /**
     * Ažurira postojeću kategoriju.
     */
    public CategoryDTO updateCategory(Long id, CategoryDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Kategorija nije pronađena sa id: " + id
                ));

        category.setName(request.name());
        category.setDescription(request.description());

        Category updated = categoryRepository.save(category);
        return toDTO(updated);
    }

    /**
     * Briše kategoriju po ID-ju.
     */
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kategorija nije pronađena sa id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    /**
     * Konvertuje Category entitet u CategoryDTO.
     * Private helper metoda — koristi se samo unutar ovog servisa.
     */
    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}

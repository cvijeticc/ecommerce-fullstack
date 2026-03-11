package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.ProductDTO;
import com.andrija.ecommerce.entity.Product;
import com.andrija.ecommerce.exception.ResourceNotFoundException;
import com.andrija.ecommerce.repository.CategoryRepository;
import com.andrija.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit testovi za ProductService.
 *
 * Šta testiramo:
 * 1. getProductById — vraća proizvod kada postoji (happy path)
 * 2. getProductById — baca ResourceNotFoundException kada ne postoji
 * 3. createProduct  — čuva i vraća novi proizvod (bez kategorije)
 * 4. deleteProduct  — briše kada postoji
 * 5. deleteProduct  — baca ResourceNotFoundException kada ne postoji
 *
 * Pattern koji koristimo u svakom testu:
 * ─ Arrange (pripremi) — postavi mock-ove i test podatke
 * ─ Act    (izvrši)    — pozovi metodu koja se testira
 * ─ Assert (proveri)   — provjeri da je rezultat ispravan
 *
 * Ovaj pattern se zove AAA (Arrange-Act-Assert) i standardan je u industiji.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    // ─── Testovi za getProductById() ──────────────────────────────────────────

    @Test
    @DisplayName("getProductById treba da vrati ProductDTO kada proizvod postoji")
    void getProductById_shouldReturnProduct_whenExists() {
        // Arrange — pravimo lažni Product koji će mock da vrati
        Product product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Apple pametni telefon")
                .price(new BigDecimal("119999.99"))
                .stockQuantity(10)
                .imageUrl("http://example.com/iphone.jpg")
                .build(); // category = null (nema kategorije)

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        ProductDTO result = productService.getProductById(1L);

        // Assert — proveravamo sva polja u DTO-u
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("iPhone 15");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("119999.99"));
        assertThat(result.stockQuantity()).isEqualTo(10);
        assertThat(result.categoryName()).isNull(); // nema kategorije
    }

    @Test
    @DisplayName("getProductById treba da baci ResourceNotFoundException kada proizvod ne postoji")
    void getProductById_shouldThrow_whenNotFound() {
        // Arrange — ID 99 ne postoji u bazi
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert — proveravamo i tip izuzetka i sadržaj poruke
        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99"); // poruka treba da sadrži ID koji nije nađen
    }

    // ─── Testovi za createProduct() ───────────────────────────────────────────

    @Test
    @DisplayName("createProduct treba da sačuva proizvod bez kategorije i vrati DTO")
    void createProduct_shouldSaveAndReturn_whenNoCategoryId() {
        // Arrange — request bez kategorije (categoryId = null)
        ProductDTO request = new ProductDTO(
                null,                           // id — null pri kreiranju
                "Samsung Galaxy S24",           // name
                "Android pametni telefon",      // description
                new BigDecimal("89999.00"),     // price
                15,                             // stockQuantity
                null,                           // imageUrl
                null,                           // categoryName (response polje, ignorišemo u requestu)
                null                            // categoryId = null → nema kategorije
        );

        // Simuliramo šta JPA save() vraća (dodeljuje ID)
        Product savedProduct = Product.builder()
                .id(5L)
                .name("Samsung Galaxy S24")
                .description("Android pametni telefon")
                .price(new BigDecimal("89999.00"))
                .stockQuantity(15)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDTO result = productService.createProduct(request);

        // Assert
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Samsung Galaxy S24");
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("89999.00"));
        assertThat(result.stockQuantity()).isEqualTo(15);

        // Verifikujemo da je save() pozvan tačno jednom
        verify(productRepository, times(1)).save(any(Product.class));
        // Verifikujemo da categoryRepository NIJE pozvan (nema categoryId)
        verify(categoryRepository, never()).findById(any());
    }

    // ─── Testovi za deleteProduct() ───────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct treba da pozove deleteById kada proizvod postoji")
    void deleteProduct_shouldDelete_whenExists() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);

        // Act
        productService.deleteProduct(1L);

        // Assert — verifikujemo da je deleteById pozvan sa tačnim ID-jem
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteProduct treba da baci ResourceNotFoundException kada proizvod ne postoji")
    void deleteProduct_shouldThrow_whenNotFound() {
        // Arrange — proizvod ne postoji
        when(productRepository.existsById(99L)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        // Verifikujemo da deleteById NIKAD nije pozvan (prekinuli smo pre brisanja)
        verify(productRepository, never()).deleteById(any());
    }
}

package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.CartItemDTO;
import com.andrija.ecommerce.entity.CartItem;
import com.andrija.ecommerce.entity.Product;
import com.andrija.ecommerce.entity.User;
import com.andrija.ecommerce.exception.ResourceNotFoundException;
import com.andrija.ecommerce.repository.CartItemRepository;
import com.andrija.ecommerce.repository.ProductRepository;
import com.andrija.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servis koji upravlja korisničkom korpom.
 *
 * Ključna sigurnosna napomena:
 * Korisnik se UVEK identifikuje iz JWT tokena (SecurityContextHolder),
 * a NIKADA iz request body-ja!
 * Ovo sprečava napad gde korisnik A menja korpu korisnika B
 * slanjem drugačijeg userId u zahtevu.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Vraća sve stavke korpe trenutno ulogovanog korisnika.
     */
    public List<CartItemDTO> getCart() {
        User currentUser = getCurrentUser();

        return cartItemRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Dodaje proizvod u korpu.
     *
     * Ako isti proizvod već postoji u korpi — povećavamo quantity.
     * Ako ne postoji — kreiramo novi CartItem.
     *
     * @param productId ID proizvoda koji dodajemo
     * @param quantity  koliko komada
     * @return DTO dodate/ažurirane stavke
     */
    public CartItemDTO addToCart(Long productId, Integer quantity) {
        User currentUser = getCurrentUser();

        // Proveravamo da li proizvod postoji
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Proizvod nije pronađen sa id: " + productId
                ));

        // Proveravamo da li isti proizvod već postoji u korpi
        var existingItem = cartItemRepository.findByUserIdAndProductId(
            currentUser.getId(), productId
        );

        if (existingItem.isPresent()) {
            // Povećavamo quantity postojeće stavke
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return toDTO(cartItemRepository.save(item));
        } else {
            // Kreiramo novu stavku korpe
            CartItem newItem = CartItem.builder()
                    .user(currentUser)
                    .product(product)
                    .quantity(quantity)
                    .build();
            return toDTO(cartItemRepository.save(newItem));
        }
    }

    /**
     * Ažurira količinu stavke u korpi.
     *
     * Sigurnosna provera: findByIdAndUserId — korisnik može menjati
     * samo SVOJE stavke, ne tuđe (čak i ako zna ID).
     *
     * @param cartItemId ID stavke korpe
     * @param quantity   nova količina
     */
    public CartItemDTO updateCartItem(Long cartItemId, Integer quantity) {
        User currentUser = getCurrentUser();

        // Tražimo stavku koja pripada OVOM korisniku (sigurnosna provera!)
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Stavka korpe nije pronađena sa id: " + cartItemId
                ));

        cartItem.setQuantity(quantity);
        return toDTO(cartItemRepository.save(cartItem));
    }

    /**
     * Uklanja jednu stavku iz korpe.
     */
    public void removeFromCart(Long cartItemId) {
        User currentUser = getCurrentUser();

        // Proveravamo da li stavka postoji I pripada ovom korisniku
        CartItem cartItem = cartItemRepository.findByIdAndUserId(cartItemId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Stavka korpe nije pronađena sa id: " + cartItemId
                ));

        cartItemRepository.delete(cartItem);
    }

    /**
     * Briše celu korpu trenutnog korisnika.
     * Poziva se i automatski pri kreiranju porudžbine.
     *
     * @Transactional — jer deleteByUserId modifikuje bazu
     */
    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        cartItemRepository.deleteByUserId(currentUser.getId());
    }

    /**
     * Pomoćna metoda: vraća trenutno ulogovanog korisnika iz SecurityContext-a.
     *
     * SecurityContextHolder je ThreadLocal — čuva Authentication objekat
     * za trenutni HTTP zahtev/thread.
     * JwtAuthenticationFilter je taj koji popunjava SecurityContext.
     */
    private User getCurrentUser() {
        // getPrincipal() vraća UserDetails — naš User implementira UserDetails
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName(); // getName() vraća username = email

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Korisnik nije pronađen"
                ));
    }

    /**
     * Konvertuje CartItem entitet u DTO za slanje klijentu.
     * Računa subtotal = price * quantity.
     */
    private CartItemDTO toDTO(CartItem cartItem) {
        BigDecimal subtotal = cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemDTO(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getImageUrl(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }
}

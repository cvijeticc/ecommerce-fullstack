package com.andrija.ecommerce.service;

import com.andrija.ecommerce.dto.OrderDTO;
import com.andrija.ecommerce.dto.OrderItemDTO;
import com.andrija.ecommerce.entity.*;
import com.andrija.ecommerce.enums.OrderStatus;
import com.andrija.ecommerce.exception.InsufficientStockException;
import com.andrija.ecommerce.exception.ResourceNotFoundException;
import com.andrija.ecommerce.repository.CartItemRepository;
import com.andrija.ecommerce.repository.OrderRepository;
import com.andrija.ecommerce.repository.ProductRepository;
import com.andrija.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servis za upravljanje porudžbinama.
 *
 * Najkompleksnija operacija: createOrder() — potpuno transakciona!
 *
 * @Transactional garantuje "sve ili ništa":
 * Ako bilo koji korak ne uspe (nema stanja, DB greška...),
 * Hibernate automatski radi ROLLBACK i baza ostaje neizmenjena.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Kreira novu porudžbinu iz sadržaja korpe.
     *
     * Detaljan tok (10 koraka, sve pod jednom @Transactional):
     *
     * 1. Identifikujemo korisnika iz JWT tokena (SecurityContext)
     * 2. Učitavamo sve stavke iz korpe tog korisnika
     * 3. Proveravamo da korpa nije prazna
     * 4. Za svaki CartItem proveravamo da ima dovoljno na stanju
     * 5. Kreiramo OrderItem-e sa priceAtPurchase (snapshot cene!)
     * 6. Računamo totalAmount = suma(quantity * priceAtPurchase)
     * 7. Smanjujemo stockQuantity za svaki naručeni proizvod
     * 8. Kreiramo Order entitet i čuvamo ga (kaskada čuva i OrderItem-e)
     * 9. Brišemo celu korpu korisnika
     *
     * @param shippingAddress adresa dostave
     * @return OrderDTO sa detaljima kreiranog narudžbine
     */
    @Transactional
    public OrderDTO createOrder(String shippingAddress) {
        // Korak 1: Ko pravi porudžbinu?
        User currentUser = getCurrentUser();

        // Korak 2: Učitavamo korpu
        List<CartItem> cartItems = cartItemRepository.findByUserId(currentUser.getId());

        // Korak 3: Prazna korpa — ne može se napraviti porudžbina
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Korpa je prazna. Dodajte proizvode pre porudžbine.");
        }

        // Korakovi 4, 5, 6, 7: Prolazimo kroz sve stavke korpe
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new java.util.ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Korak 4: Provera stanja — ima li dovoljno?
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                    "Nema dovoljno na stanju za: '" + product.getName() +
                    "'. Dostupno: " + product.getStockQuantity() +
                    ", traženo: " + cartItem.getQuantity()
                );
            }

            // Korak 5: Kreiramo OrderItem
            // KLJUČNO: priceAtPurchase = trenutna cena — čuva istorijsku cenu!
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice()) // snapshot cene!
                    .build();

            orderItems.add(orderItem);

            // Korak 6: Dodajemo na ukupan iznos
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Korak 7: Smanjujemo stock (direktna modifikacija entiteta — Dirty Checking)
            // Hibernate detektuje promenu i automatski generiše UPDATE SQL pri commit-u
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Korak 8: Kreiramo porudžbinu
        Order order = Order.builder()
                .user(currentUser)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING) // nova porudžbina uvek počinje kao PENDING
                .shippingAddress(shippingAddress)
                .items(orderItems)
                .build();

        // Postavljamo referencu nazad na Order (potrebno zbog bidirekcione veze)
        orderItems.forEach(item -> item.setOrder(order));

        // save(order) → kaskada (CascadeType.ALL) automatski čuva i sve OrderItem-e!
        Order savedOrder = orderRepository.save(order);

        // Korak 9: Brisanje korpe — ona je sada pretvorena u porudžbinu
        cartItemRepository.deleteByUserId(currentUser.getId());

        return toDTO(savedOrder);
    }

    /**
     * Vraća sve porudžbine trenutno ulogovanog korisnika.
     * Sortirane od najnovije ka najstarijoj.
     */
    public List<OrderDTO> getMyOrders() {
        User currentUser = getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Vraća detalje jedne porudžbine korisnika.
     *
     * findByIdAndUserId — sigurnosna provera!
     * Korisnik može videti samo SVOJU porudžbinu, ne tuđu.
     */
    public OrderDTO getMyOrderById(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Porudžbina nije pronađena sa id: " + orderId
                ));
        return toDTO(order);
    }

    /**
     * Admin: vraća SVE porudžbine od SVIH korisnika.
     *
     * @Transactional(readOnly = true) — drži Hibernate sesiju otvorenu dok se
     * učitavaju LAZY relacije (order.getUser(), order.getItems()).
     * readOnly = true je optimizacija za SELECT operacije.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Admin: menja status porudžbine.
     * Npr. PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Porudžbina nije pronađena sa id: " + orderId
                ));

        order.setStatus(newStatus);
        return toDTO(orderRepository.save(order));
    }

    /**
     * Vraća trenutno ulogovanog korisnika iz SecurityContext-a.
     * Isti princip kao u CartService-u.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik nije pronađen"));
    }

    /**
     * Konvertuje Order entitet u OrderDTO.
     * Rekurzivno konvertuje i sve OrderItem-e u OrderItemDTO-ove.
     */
    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems() == null ? List.of() :
                order.getItems().stream()
                        .map(this::toItemDTO)
                        .toList();

        return new OrderDTO(
                order.getId(),
                order.getUser().getEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                itemDTOs,
                order.getCreatedAt()
        );
    }

    /**
     * Konvertuje OrderItem u OrderItemDTO.
     * Računa subtotal = priceAtPurchase * quantity.
     */
    private OrderItemDTO toItemDTO(OrderItem item) {
        BigDecimal subtotal = item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtPurchase(),
                subtotal
        );
    }
}

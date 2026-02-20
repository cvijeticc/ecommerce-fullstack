package com.andrija.ecommerce.exception;

/**
 * Baca se kada korisnik pokuša da naruči više komada nego što ima na stanju.
 * Mapira se na HTTP 400 Bad Request u GlobalExceptionHandler-u.
 *
 * Primer upotrebe:
 *   if (product.getStockQuantity() < cartItem.getQuantity()) {
 *       throw new InsufficientStockException(
 *           "Nema dovoljno na stanju za: " + product.getName() +
 *           ". Dostupno: " + product.getStockQuantity()
 *       );
 *   }
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}

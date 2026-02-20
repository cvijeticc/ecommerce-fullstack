package com.andrija.ecommerce.exception;

/**
 * Baca se kada traženi resurs nije pronađen u bazi.
 * Mapira se na HTTP 404 Not Found u GlobalExceptionHandler-u.
 *
 * Primer upotrebe:
 *   Product product = productRepository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException("Proizvod nije pronađen sa id: " + id));
 *
 * Extends RuntimeException — ne moramo da deklarišemo "throws" u signaturi metode.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

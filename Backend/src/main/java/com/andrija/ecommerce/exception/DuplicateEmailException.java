package com.andrija.ecommerce.exception;

/**
 * Baca se kada korisnik pokušava da se registruje sa emailom koji već postoji.
 * Mapira se na HTTP 409 Conflict u GlobalExceptionHandler-u.
 *
 * Primer upotrebe:
 *   if (userRepository.existsByEmail(request.email())) {
 *       throw new DuplicateEmailException("Korisnik sa emailom " + request.email() + " već postoji");
 *   }
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}

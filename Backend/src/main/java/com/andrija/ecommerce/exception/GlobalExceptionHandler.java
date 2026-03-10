package com.andrija.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralizovano rukovanje izuzecima za celu aplikaciju.
 *
 * @RestControllerAdvice — "presreće" izuzetke iz svih kontrolera.
 * Umesto da svaki kontroler ima try-catch blokove, ovde definišemo
 * šta se vraća klijentu za svaki tip greške.
 *
 * Bez ovoga, Spring bi vratio generičku 500 Internal Server Error poruku
 * koja ne govori frontendu šta je pošlo naopako.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found — resurs nije pronađen.
     * Primer: tražen proizvod, kategorija ili porudžbina ne postoji.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 409 Conflict — konflikt podataka.
     * Primer: email već postoji pri registraciji.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 400 Bad Request — nedovoljno na stanju.
     * Primer: korisnik želi 5 komada, ali ima samo 2 na stanju.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 400 Bad Request — validacija @Valid nije prošla.
     * Primer: email format nije ispravan, lozinka prekratka, obavezno polje prazno...
     *
     * MethodArgumentNotValidException se automatski baca kada @Valid anotacija
     * u kontroleru otkrije neispravne podatke.
     *
     * Vraćamo map sa svim greškama: { "email": "Email format nije ispravan", "firstName": "..." }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        // Prolazimo kroz sve greške validacije i mapiramo polje -> poruka greške
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validacija nije prošla");
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 409 Conflict — operacija nije dozvoljena zbog trenutnog stanja podataka.
     * Primer: pokušaj brisanja kategorije koja ima proizvode.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 500 Internal Server Error — neočekivana greška.
     * Hvatamo sve ostale izuzetke da aplikacija ne "padne" bez poruke.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Interna greška servera: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Interni record koji definiše format JSON odgovora pri grešci.
     * Svaka greška vraća: { "status": 404, "message": "...", "timestamp": "..." }
     */
    public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
}

package md.impact.ecommerce.exceptions;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("status", ex.getStatus().value(), "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Date invalide");
        return ResponseEntity.badRequest()
                .body(Map.of("status", 400, "message", message));
    }

    // Lecția 10 — înainte de acest handler, un parametru cu tipul greșit (ex.
    // ?category=1%20OR%201=1, trimis de un scanner automat sau un atacator care
    // testează SQL injection) ajungea la handler-ul implicit din Spring: un 400
    // cu corp GOL, fără mesajul curat pe care restul API-ului îl întoarce mereu.
    // Nu era o gaură de securitate în sine (Spring respinge oricum request-ul
    // înainte să ajungă la orice query SQL — parametrii JPA sunt mereu legați,
    // niciodată concatenați), dar era o inconsecvență care putea scurge detalii
    // de implementare dacă cineva ar fi dezactivat vreodată acest handler.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("status", 400, "message", "Parametru invalid: " + ex.getName()));
    }
}

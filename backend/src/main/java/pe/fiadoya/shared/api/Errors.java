package pe.fiadoya.shared.api;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class Errors {
  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentNotValidException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class
  })
  ResponseEntity<?> bad(Exception e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "message",
                e instanceof IllegalArgumentException
                    ? e.getMessage()
                    : "Revise los campos y formatos del formulario"));
  }

  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<?> missing(Exception e) {
    return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<?> denied() {
    return ResponseEntity.status(403)
        .body(Map.of("message", "No tiene autorización para este recurso"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict() {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "message", "Registro duplicado o conflicto de integridad. Actualice la consulta."));
  }
}

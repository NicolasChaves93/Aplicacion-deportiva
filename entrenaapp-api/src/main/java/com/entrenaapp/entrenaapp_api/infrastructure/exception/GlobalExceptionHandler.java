package com.entrenaapp.entrenaapp_api.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Deliberadamente minimo: solo mapea RecursoNoEncontradoException a 404. El
// resto de RuntimeException (documento duplicado, credenciales invalidas,
// etc.) se dejan tal cual venian cayendo en 500, para no cambiar
// comportamiento ya probado fuera de lo que la sincronizacion de bajada
// necesita.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}

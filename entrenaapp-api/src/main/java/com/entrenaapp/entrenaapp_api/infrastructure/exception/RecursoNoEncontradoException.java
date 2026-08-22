package com.entrenaapp.entrenaapp_api.infrastructure.exception;

// Se traduce a HTTP 404 (ver GlobalExceptionHandler). Distinta de una
// RuntimeException generica para que el celular pueda diferenciar "este
// registro no existe" de un error transitorio del servidor al reconciliar
// la sincronizacion de bajada (ver SyncRepository en la app movil).
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

package com.mesaayuda.shared.excepcion;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> manejarParametroInvalido(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String mensaje = "El valor '%s' no es válido para el parámetro '%s'".formatted(ex.getValue(), ex.getName());
        return construir(HttpStatus.BAD_REQUEST, mensaje, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarError(Exception ex, WebRequest request) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", request, null);
    }

    private ResponseEntity<ApiError> construir(HttpStatus status, String mensaje, WebRequest request, String codigoError) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getDescription(false).replace("uri=", ""),
                codigoError);
        return ResponseEntity.status(status).body(error);
    }
}

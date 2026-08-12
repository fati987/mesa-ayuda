package com.mesaayuda.auth;

import org.springframework.security.access.AccessDeniedException;

/**
 * Extiende la AccessDeniedException de Spring Security (no RuntimeException
 * genérica) para reutilizar el mismo @ExceptionHandler(AccessDeniedException.class)
 * que ya cubre los fallos de @PreAuthorize: un solo handler, un solo
 * formato de ApiError para todo 403.
 */
public class AccesoAreaDenegadoException extends AccessDeniedException {

    public AccesoAreaDenegadoException() {
        super("El área de su usuario no coincide con el área actual del ticket");
    }
}

package com.mesaayuda.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Punto único de acceso al usuario autenticado actual, para que los
 * servicios que lo necesitan (ej. TicketService) queden testeables con
 * Mockito en vez de depender directamente de SecurityContextHolder.
 */
@Component
public class UsuarioActualProvider {

    public UsuarioPrincipal actual() {
        return (UsuarioPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

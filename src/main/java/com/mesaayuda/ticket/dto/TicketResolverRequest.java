package com.mesaayuda.ticket.dto;

/**
 * solucion no es @NotBlank a nivel de DTO a propósito: la guarda que la
 * exige es específica de la transición EN_PROGRESO->RESUELTO
 * (TransicionService la valida ahí); NUEVO->RESUELTO no la requiere según
 * la tabla de estados del CLAUDE.md.
 */
public record TicketResolverRequest(String solucion) {
}

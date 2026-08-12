package com.mesaayuda.ticket.dto;

import java.time.Instant;

import com.mesaayuda.ticket.enums.EstadoTicket;

public record HistorialEstadoDto(
        EstadoTicket estadoAnterior,
        EstadoTicket estadoNuevo,
        String usuarioNombre,
        String comentario,
        Instant creadoEn) {
}

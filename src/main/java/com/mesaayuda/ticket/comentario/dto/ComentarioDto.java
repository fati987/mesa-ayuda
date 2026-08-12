package com.mesaayuda.ticket.comentario.dto;

import java.time.Instant;

import com.mesaayuda.ticket.enums.Visibilidad;

public record ComentarioDto(
        String autorNombre,
        Visibilidad visibilidad,
        String contenido,
        Instant creadoEn) {
}

package com.mesaayuda.ticket.comentario.dto;

import com.mesaayuda.ticket.enums.Visibilidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComentarioCrearRequest(
        @NotNull Visibilidad visibilidad,
        @NotBlank String contenido) {
}

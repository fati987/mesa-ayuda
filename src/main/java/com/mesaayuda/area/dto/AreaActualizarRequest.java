package com.mesaayuda.area.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AreaActualizarRequest(
        @NotBlank String nombre,
        @NotNull Boolean recibeLlamadas,
        @Positive int limiteWipAgente,
        boolean activo) {
}

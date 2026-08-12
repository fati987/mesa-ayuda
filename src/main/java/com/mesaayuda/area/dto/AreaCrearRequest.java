package com.mesaayuda.area.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AreaCrearRequest(
        @NotBlank String nombre,
        @NotNull Boolean recibeLlamadas,
        @Positive int limiteWipAgente) {
}

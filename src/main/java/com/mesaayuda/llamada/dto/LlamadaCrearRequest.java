package com.mesaayuda.llamada.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record LlamadaCrearRequest(
        @NotBlank String contactoTelefono,
        @NotBlank String contactoNombreCompleto,
        @Email String contactoCorreo,
        @Positive Integer duracionSegundos) {
}

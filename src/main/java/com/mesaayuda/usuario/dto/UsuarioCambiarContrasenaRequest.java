package com.mesaayuda.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCambiarContrasenaRequest(
        @NotBlank @Size(min = 8) String contrasenaNueva) {
}

package com.mesaayuda.usuario.dto;

import com.mesaayuda.usuario.enums.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioActualizarRequest(
        @NotBlank String nombreCompleto,
        @NotNull Rol rol,
        @NotNull Long areaId,
        boolean activo) {
}

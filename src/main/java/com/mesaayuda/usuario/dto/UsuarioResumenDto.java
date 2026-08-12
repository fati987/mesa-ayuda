package com.mesaayuda.usuario.dto;

import com.mesaayuda.usuario.enums.Rol;

public record UsuarioResumenDto(
        String nombreCompleto,
        String correo,
        Rol rol,
        String areaNombre,
        boolean activo) {
}

package com.mesaayuda.auth.dto;

import com.mesaayuda.usuario.enums.Rol;

public record UsuarioActualDto(
        String nombreCompleto,
        String correo,
        Rol rol,
        Long areaId,
        String areaNombre) {
}

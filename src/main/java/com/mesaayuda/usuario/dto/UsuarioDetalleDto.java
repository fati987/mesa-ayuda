package com.mesaayuda.usuario.dto;

import java.time.Instant;

import com.mesaayuda.usuario.enums.Rol;

public record UsuarioDetalleDto(
        String nombreCompleto,
        String correo,
        Rol rol,
        String areaNombre,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn) {
}

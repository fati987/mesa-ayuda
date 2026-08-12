package com.mesaayuda.usuario.dto;

import com.mesaayuda.usuario.enums.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCrearRequest(
        @NotBlank String nombreCompleto,
        @NotBlank @Email String correo,
        @NotNull Rol rol,
        @NotNull Long areaId,
        @NotBlank @Size(min = 8) String contrasena) {
}

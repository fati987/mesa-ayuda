package com.mesaayuda.categoria.dto;

public record CategoriaDto(
        Long id,
        String nombre,
        String descripcion,
        boolean activo) {
}

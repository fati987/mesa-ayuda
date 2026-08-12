package com.mesaayuda.area.dto;

public record AreaDto(
        Long id,
        String nombre,
        boolean recibeLlamadas,
        int limiteWipAgente,
        boolean activo) {
}

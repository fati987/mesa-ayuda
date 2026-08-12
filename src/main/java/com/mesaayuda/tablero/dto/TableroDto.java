package com.mesaayuda.tablero.dto;

import java.util.List;

public record TableroDto(
        Long areaId,
        String areaNombre,
        List<ColumnaTableroDto> columnas) {
}

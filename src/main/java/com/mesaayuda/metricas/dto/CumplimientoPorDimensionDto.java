package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;

public record CumplimientoPorDimensionDto(
        String nombre,
        long total,
        long cumplidos,
        long incumplidos,
        long enCurso,
        BigDecimal porcentajeCumplimiento) {
}

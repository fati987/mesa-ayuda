package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CumplimientoSlaDto(
        LocalDate desde,
        LocalDate hasta,
        long total,
        long cumplidos,
        long incumplidos,
        long enCurso,
        BigDecimal porcentajeCumplimiento,
        List<CumplimientoPorDimensionDto> porCategoria,
        List<CumplimientoPorDimensionDto> porArea,
        List<CumplimientoPorDimensionDto> porPrioridad) {
}

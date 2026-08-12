package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FcrResumenDto(
        LocalDate desde,
        LocalDate hasta,
        long totalCreados,
        long resueltosEnLlamada,
        BigDecimal porcentajeFcr,
        List<FcrPorAreaDto> porArea,
        List<FcrPorDiaDto> porDia) {
}

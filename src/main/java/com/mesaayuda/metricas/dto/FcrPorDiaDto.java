package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FcrPorDiaDto(
        LocalDate dia,
        long totalCreados,
        long resueltosEnLlamada,
        BigDecimal porcentajeFcr) {
}

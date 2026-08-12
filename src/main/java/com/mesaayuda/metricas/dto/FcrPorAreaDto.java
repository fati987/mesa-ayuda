package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;

public record FcrPorAreaDto(
        Long areaId,
        String areaNombre,
        long totalCreados,
        long resueltosEnLlamada,
        BigDecimal porcentajeFcr) {
}

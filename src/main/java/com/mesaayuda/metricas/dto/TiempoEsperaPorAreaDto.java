package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;

public record TiempoEsperaPorAreaDto(
        Long areaId,
        String areaNombre,
        long tomasConsideradas,
        BigDecimal esperaPromedioMinutos) {
}

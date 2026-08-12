package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;

public record TasaDerivacionPorAreaDto(
        Long areaId,
        String areaNombre,
        long totalCreados,
        long derivadosAlMenosUnaVez,
        BigDecimal tasaDerivacion) {
}

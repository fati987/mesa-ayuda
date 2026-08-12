package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TiempoEsperaResumenDto(
        LocalDate desde,
        LocalDate hasta,
        long tomasConsideradas,
        BigDecimal esperaPromedioMinutos,
        BigDecimal esperaMedianaMinutos,
        List<TiempoEsperaPorAreaDto> porArea) {
}

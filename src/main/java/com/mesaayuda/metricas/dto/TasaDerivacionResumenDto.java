package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TasaDerivacionResumenDto(
        LocalDate desde,
        LocalDate hasta,
        long totalCreados,
        long derivadosAlMenosUnaVez,
        BigDecimal tasaDerivacion,
        long ticketsRebotados,
        BigDecimal tasaRebote,
        List<TasaDerivacionPorAreaDto> porArea) {
}

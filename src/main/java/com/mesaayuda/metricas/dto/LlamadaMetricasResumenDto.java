package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LlamadaMetricasResumenDto(
        LocalDate desde,
        LocalDate hasta,
        long totalLlamadas,
        BigDecimal duracionPromedioSegundos,
        long llamadasConMultiplesTickets,
        BigDecimal porcentajeMultiplesTickets) {
}

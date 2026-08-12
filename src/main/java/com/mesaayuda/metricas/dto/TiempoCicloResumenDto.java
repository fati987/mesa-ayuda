package com.mesaayuda.metricas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TiempoCicloResumenDto(
        LocalDate desde,
        LocalDate hasta,
        long ticketsLeadTime,
        BigDecimal leadTimePromedioMinutos,
        BigDecimal leadTimeMedianoMinutos,
        long ticketsCycleTime,
        BigDecimal cycleTimePromedioMinutos,
        BigDecimal cycleTimeMedianoMinutos) {
}

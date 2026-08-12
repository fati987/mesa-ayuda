package com.mesaayuda.metricas.dto;

import java.time.LocalDate;

import com.mesaayuda.ticket.enums.EstadoTicket;

public record FlujoAcumuladoPuntoDto(
        LocalDate dia,
        EstadoTicket estado,
        long cantidad) {
}

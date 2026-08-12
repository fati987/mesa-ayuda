package com.mesaayuda.metricas.dto;

import java.time.Instant;

public record LlamadaMultiTicketDto(
        Long llamadaId,
        Instant fechaHora,
        String contactoNombre,
        long cantidadTickets) {
}

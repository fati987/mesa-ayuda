package com.mesaayuda.ticket.dto;

import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketCrearRequest(
        @NotNull Long llamadaId,
        @NotNull Long areaId,
        @NotNull Long categoriaId,
        @NotNull TipoTicket tipo,
        @NotNull Prioridad prioridad,
        @NotNull Urgencia urgencia,
        @NotNull Impacto impacto,
        @NotBlank String titulo,
        @NotBlank String descripcion,
        boolean resueltoEnLlamada,
        String solucion) {
}

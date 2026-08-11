package com.mesaayuda.ticket.dto;

import java.time.Instant;

import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;

public record TicketResumenDto(
        String codigo,
        String titulo,
        TipoTicket tipo,
        EstadoTicket estado,
        Prioridad prioridad,
        Urgencia urgencia,
        Impacto impacto,
        String areaActualNombre,
        String categoriaNombre,
        String contactoNombre,
        boolean resueltoEnLlamada,
        Instant creadoEn,
        Instant actualizadoEn) {
}

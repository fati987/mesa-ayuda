package com.mesaayuda.ticket.dto;

import java.time.Instant;
import java.util.List;

import com.mesaayuda.contacto.dto.ContactoResumenDto;
import com.mesaayuda.llamada.dto.LlamadaResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Origen;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;

public record TicketDetalleDto(
        String codigo,
        String titulo,
        String descripcion,
        TipoTicket tipo,
        EstadoTicket estado,
        Prioridad prioridad,
        Urgencia urgencia,
        Impacto impacto,
        Origen origen,
        String areaActualNombre,
        String categoriaNombre,
        String usuarioCreadorNombre,
        String usuarioAsignadoNombre,
        boolean resueltoEnLlamada,
        String solucion,
        int minutosPausado,
        ContactoResumenDto contacto,
        LlamadaResumenDto llamada,
        List<HistorialEstadoDto> historial,
        Instant fechaVencimiento,
        boolean escalado,
        Instant creadoEn,
        Instant actualizadoEn) {
}

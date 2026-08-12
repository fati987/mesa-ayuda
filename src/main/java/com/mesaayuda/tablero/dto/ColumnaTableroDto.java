package com.mesaayuda.tablero.dto;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;

public record ColumnaTableroDto(
        Long id,
        String nombre,
        EstadoTicket estadoAsociado,
        int orden,
        PaginaResponse<TicketResumenDto> tickets) {
}

package com.mesaayuda.tablero.mapper;

import org.springframework.data.domain.Page;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.tablero.ColumnaTablero;
import com.mesaayuda.tablero.dto.ColumnaTableroDto;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.mapper.TicketMapper;

public final class TableroMapper {

    private TableroMapper() {
    }

    public static ColumnaTableroDto aColumnaDto(ColumnaTablero columna, Page<Ticket> tickets) {
        PaginaResponse<TicketResumenDto> pagina = PaginaResponse.de(tickets, TicketMapper::aResumen);
        return new ColumnaTableroDto(columna.getId(), columna.getNombre(), columna.getEstadoAsociado(), columna.getOrden(), pagina);
    }
}

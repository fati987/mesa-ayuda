package com.mesaayuda.ticket;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.mapper.TicketMapper;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public PaginaResponse<TicketResumenDto> listar(EstadoTicket estado, Prioridad prioridad, Long areaId, Pageable pageable) {
        return PaginaResponse.de(ticketRepository.buscar(estado, prioridad, areaId, pageable), TicketMapper::aResumen);
    }

    public TicketDetalleDto obtenerPorCodigo(String codigo) {
        Ticket ticket = ticketRepository.findByCodigoAndEliminadoEnIsNull(codigo)
                .orElseThrow(() -> new TicketNoEncontradoException(codigo));
        return TicketMapper.aDetalle(ticket);
    }
}

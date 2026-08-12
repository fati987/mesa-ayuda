package com.mesaayuda.ticket;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.auth.AccesoAreaValidator;
import com.mesaayuda.auth.UsuarioActualProvider;
import com.mesaayuda.auth.UsuarioPrincipal;
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
    private final UsuarioActualProvider usuarioActualProvider;
    private final AccesoAreaValidator accesoAreaValidator;

    public TicketService(TicketRepository ticketRepository, UsuarioActualProvider usuarioActualProvider,
            AccesoAreaValidator accesoAreaValidator) {
        this.ticketRepository = ticketRepository;
        this.usuarioActualProvider = usuarioActualProvider;
        this.accesoAreaValidator = accesoAreaValidator;
    }

    public PaginaResponse<TicketResumenDto> listar(EstadoTicket estado, Prioridad prioridad, Long areaId, Pageable pageable) {
        UsuarioPrincipal actual = usuarioActualProvider.actual();
        Long areaEfectiva = accesoAreaValidator.resolverFiltroArea(actual, areaId);
        return PaginaResponse.de(ticketRepository.buscar(estado, prioridad, areaEfectiva, pageable), TicketMapper::aResumen);
    }

    public TicketDetalleDto obtenerPorCodigo(String codigo) {
        Ticket ticket = ticketRepository.findByCodigoAndEliminadoEnIsNull(codigo)
                .orElseThrow(() -> new TicketNoEncontradoException(codigo));
        accesoAreaValidator.verificarAcceso(usuarioActualProvider.actual(), ticket.getAreaActual().getId());
        return TicketMapper.aDetalle(ticket);
    }
}

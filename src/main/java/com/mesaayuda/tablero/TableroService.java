package com.mesaayuda.tablero;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaNoEncontradaException;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.auth.AccesoAreaValidator;
import com.mesaayuda.auth.UsuarioActualProvider;
import com.mesaayuda.auth.UsuarioPrincipal;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.tablero.dto.ColumnaTableroDto;
import com.mesaayuda.tablero.dto.TableroDto;
import com.mesaayuda.tablero.mapper.TableroMapper;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.TicketRepository;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.mapper.TicketMapper;

@Service
@Transactional(readOnly = true)
public class TableroService {

    private static final int TAMANO_PAGINA_COLUMNA_DEFAULT = 10;

    private final ColumnaTableroRepository columnaTableroRepository;
    private final AreaRepository areaRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioActualProvider usuarioActualProvider;
    private final AccesoAreaValidator accesoAreaValidator;

    public TableroService(ColumnaTableroRepository columnaTableroRepository, AreaRepository areaRepository,
            TicketRepository ticketRepository, UsuarioActualProvider usuarioActualProvider,
            AccesoAreaValidator accesoAreaValidator) {
        this.columnaTableroRepository = columnaTableroRepository;
        this.areaRepository = areaRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioActualProvider = usuarioActualProvider;
        this.accesoAreaValidator = accesoAreaValidator;
    }

    public TableroDto obtener(Long areaIdSolicitada, Integer tamanoPagina) {
        UsuarioPrincipal actual = usuarioActualProvider.actual();
        Long areaId = accesoAreaValidator.resolverFiltroArea(actual, areaIdSolicitada);
        if (areaId == null) {
            throw new AreaTableroRequeridaException();
        }
        Area area = areaRepository.findById(areaId).orElseThrow(() -> new AreaNoEncontradaException(areaId));

        int tamano = tamanoPagina != null ? tamanoPagina : TAMANO_PAGINA_COLUMNA_DEFAULT;
        List<ColumnaTableroDto> columnas = columnaTableroRepository.findByAreaIdAndActivoTrueOrderByOrdenAsc(areaId).stream()
                .map(columna -> {
                    Page<Ticket> tickets = ticketRepository.buscarParaTablero(areaId, columna.getEstadoAsociado(), PageRequest.of(0, tamano));
                    return TableroMapper.aColumnaDto(columna, tickets);
                })
                .toList();

        return new TableroDto(area.getId(), area.getNombre(), columnas);
    }

    public PaginaResponse<TicketResumenDto> ticketsDeColumna(Long columnaId, Pageable pageable) {
        ColumnaTablero columna = columnaTableroRepository.findById(columnaId)
                .orElseThrow(() -> new ColumnaTableroNoEncontradaException(columnaId));
        accesoAreaValidator.verificarAcceso(usuarioActualProvider.actual(), columna.getArea().getId());
        Page<Ticket> tickets = ticketRepository.buscarParaTablero(columna.getArea().getId(), columna.getEstadoAsociado(), pageable);
        return PaginaResponse.de(tickets, TicketMapper::aResumen);
    }
}

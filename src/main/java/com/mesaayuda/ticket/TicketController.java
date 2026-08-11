package com.mesaayuda.ticket;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Prioridad;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public PaginaResponse<TicketResumenDto> listar(
            @RequestParam(required = false) EstadoTicket estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long areaId,
            @PageableDefault(size = 20) @SortDefault(sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return ticketService.listar(estado, prioridad, areaId, pageable);
    }

    @GetMapping("/{codigo}")
    public TicketDetalleDto obtener(@PathVariable String codigo) {
        return ticketService.obtenerPorCodigo(codigo);
    }
}

package com.mesaayuda.ticket;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.dto.TicketCrearRequest;
import com.mesaayuda.ticket.dto.TicketDerivarRequest;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResolverRequest;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Prioridad;

import jakarta.validation.Valid;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetalleDto crear(@Valid @RequestBody TicketCrearRequest request) {
        return ticketService.crear(request);
    }

    @PatchMapping("/{codigo}/derivacion")
    public TicketDetalleDto derivar(@PathVariable String codigo, @Valid @RequestBody TicketDerivarRequest request) {
        return ticketService.derivar(codigo, request);
    }

    @PatchMapping("/{codigo}/toma")
    public TicketDetalleDto tomar(@PathVariable String codigo) {
        return ticketService.tomar(codigo);
    }

    @PatchMapping("/{codigo}/resolucion")
    public TicketDetalleDto resolver(@PathVariable String codigo, @Valid @RequestBody TicketResolverRequest request) {
        return ticketService.resolver(codigo, request);
    }

    @PatchMapping("/{codigo}/espera")
    public TicketDetalleDto pausar(@PathVariable String codigo) {
        return ticketService.pausar(codigo);
    }

    @PatchMapping("/{codigo}/reanudacion")
    public TicketDetalleDto reanudar(@PathVariable String codigo) {
        return ticketService.reanudar(codigo);
    }

    @PatchMapping("/{codigo}/cierre")
    public TicketDetalleDto cerrar(@PathVariable String codigo) {
        return ticketService.cerrar(codigo);
    }

    @PatchMapping("/{codigo}/reapertura")
    public TicketDetalleDto reabrir(@PathVariable String codigo) {
        return ticketService.reabrir(codigo);
    }
}

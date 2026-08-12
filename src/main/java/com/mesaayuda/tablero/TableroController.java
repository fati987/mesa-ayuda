package com.mesaayuda.tablero;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.tablero.dto.TableroDto;
import com.mesaayuda.ticket.dto.TicketResumenDto;

@RestController
public class TableroController {

    private final TableroService tableroService;

    public TableroController(TableroService tableroService) {
        this.tableroService = tableroService;
    }

    @GetMapping("/api/tablero")
    public TableroDto obtener(
            @RequestParam(required = false) Long area,
            @RequestParam(required = false) Integer tamano) {
        return tableroService.obtener(area, tamano);
    }

    @GetMapping("/api/tablero/columnas/{columnaId}/tickets")
    public PaginaResponse<TicketResumenDto> ticketsDeColumna(
            @PathVariable Long columnaId,
            @PageableDefault(size = 20) Pageable pageable) {
        return tableroService.ticketsDeColumna(columnaId, pageable);
    }
}

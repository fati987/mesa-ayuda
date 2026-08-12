package com.mesaayuda.ticket.comentario;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.comentario.dto.ComentarioCrearRequest;
import com.mesaayuda.ticket.comentario.dto.ComentarioDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets/{codigo}/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public PaginaResponse<ComentarioDto> listar(
            @PathVariable String codigo,
            @PageableDefault(size = 20) @SortDefault(sort = "creadoEn", direction = Sort.Direction.ASC) Pageable pageable) {
        return comentarioService.listar(codigo, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioDto crear(@PathVariable String codigo, @Valid @RequestBody ComentarioCrearRequest request) {
        return comentarioService.crear(codigo, request);
    }
}

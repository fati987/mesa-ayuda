package com.mesaayuda.area;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.area.dto.AreaActualizarRequest;
import com.mesaayuda.area.dto.AreaCrearRequest;
import com.mesaayuda.area.dto.AreaDto;
import com.mesaayuda.shared.paginacion.PaginaResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @GetMapping
    public PaginaResponse<AreaDto> listar(@PageableDefault(size = 20) Pageable pageable) {
        return areaService.listar(pageable);
    }

    @GetMapping("/{id}")
    public AreaDto obtener(@PathVariable Long id) {
        return areaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AreaDto crear(@Valid @RequestBody AreaCrearRequest request) {
        return areaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AreaDto actualizar(@PathVariable Long id, @Valid @RequestBody AreaActualizarRequest request) {
        return areaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void desactivar(@PathVariable Long id) {
        areaService.desactivar(id);
    }
}

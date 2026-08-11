package com.mesaayuda.categoria;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.categoria.dto.CategoriaDto;
import com.mesaayuda.shared.paginacion.PaginaResponse;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public PaginaResponse<CategoriaDto> listar(
            @PageableDefault(size = 20) @SortDefault(sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return categoriaService.listar(pageable);
    }
}

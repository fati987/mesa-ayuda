package com.mesaayuda.categoria;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.categoria.dto.CategoriaDto;
import com.mesaayuda.categoria.mapper.CategoriaMapper;
import com.mesaayuda.shared.paginacion.PaginaResponse;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public PaginaResponse<CategoriaDto> listar(Pageable pageable) {
        return PaginaResponse.de(categoriaRepository.findByActivoTrue(pageable), CategoriaMapper::aDto);
    }
}

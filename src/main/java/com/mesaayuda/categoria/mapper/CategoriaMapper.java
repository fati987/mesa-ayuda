package com.mesaayuda.categoria.mapper;

import com.mesaayuda.categoria.Categoria;
import com.mesaayuda.categoria.dto.CategoriaDto;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaDto aDto(Categoria categoria) {
        return new CategoriaDto(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(), categoria.isActivo());
    }
}

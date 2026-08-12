package com.mesaayuda.categoria;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class CategoriaNoEncontradaException extends RecursoNoEncontradoException {

    public CategoriaNoEncontradaException(Long id) {
        super("No se encontró la categoría con id %d".formatted(id));
    }
}

package com.mesaayuda.area;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class AreaNoEncontradaException extends RecursoNoEncontradoException {

    public AreaNoEncontradaException(Long id) {
        super("No se encontró el área con id %d".formatted(id));
    }
}

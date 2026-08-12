package com.mesaayuda.tablero;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class ColumnaTableroNoEncontradaException extends RecursoNoEncontradoException {

    public ColumnaTableroNoEncontradaException(Long id) {
        super("No se encontró la columna de tablero con id %d".formatted(id));
    }
}

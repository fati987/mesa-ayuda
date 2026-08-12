package com.mesaayuda.llamada;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class LlamadaNoEncontradaException extends RecursoNoEncontradoException {

    public LlamadaNoEncontradaException(Long id) {
        super("No se encontró la llamada con id %d".formatted(id));
    }
}

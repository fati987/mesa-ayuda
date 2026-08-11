package com.mesaayuda.shared.excepcion;

public abstract class RecursoNoEncontradoException extends RuntimeException {

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

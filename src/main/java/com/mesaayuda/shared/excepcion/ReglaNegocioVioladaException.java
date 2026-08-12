package com.mesaayuda.shared.excepcion;

public abstract class ReglaNegocioVioladaException extends RuntimeException {

    protected ReglaNegocioVioladaException(String mensaje) {
        super(mensaje);
    }
}

package com.mesaayuda.shared.excepcion;

public abstract class AutenticacionFallidaException extends RuntimeException {

    protected AutenticacionFallidaException(String mensaje) {
        super(mensaje);
    }
}

package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class DerivacionInvalidaException extends ReglaNegocioVioladaException {

    private DerivacionInvalidaException(String mensaje) {
        super(mensaje);
    }

    public static DerivacionInvalidaException areaDestinoIgualALaActual() {
        return new DerivacionInvalidaException("El área destino debe ser distinta del área actual del ticket");
    }

    public static DerivacionInvalidaException motivoVacio() {
        return new DerivacionInvalidaException("El motivo de la derivación es obligatorio");
    }
}

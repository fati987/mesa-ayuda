package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class SolucionRequeridaException extends ReglaNegocioVioladaException {

    public SolucionRequeridaException() {
        super("Se requiere registrar la solución para resolver el ticket");
    }
}

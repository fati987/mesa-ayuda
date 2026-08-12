package com.mesaayuda.usuario;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class AutodesactivacionNoPermitidaException extends ReglaNegocioVioladaException {

    public AutodesactivacionNoPermitidaException() {
        super("Un usuario no puede desactivarse a sí mismo");
    }
}

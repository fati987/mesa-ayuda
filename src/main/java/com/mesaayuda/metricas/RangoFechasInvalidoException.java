package com.mesaayuda.metricas;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class RangoFechasInvalidoException extends ReglaNegocioVioladaException {

    public RangoFechasInvalidoException(String mensaje) {
        super(mensaje);
    }
}

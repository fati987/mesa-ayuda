package com.mesaayuda.tablero;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class AreaTableroRequeridaException extends ReglaNegocioVioladaException {

    public AreaTableroRequeridaException() {
        super("Un supervisor o admin debe indicar el parámetro area para consultar el tablero");
    }
}

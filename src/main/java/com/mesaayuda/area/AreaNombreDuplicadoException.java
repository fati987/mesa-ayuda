package com.mesaayuda.area;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class AreaNombreDuplicadoException extends ReglaNegocioVioladaException {

    public AreaNombreDuplicadoException(String nombre) {
        super("Ya existe un área con el nombre %s".formatted(nombre));
    }
}

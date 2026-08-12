package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class AreaNoRecibeLlamadasException extends ReglaNegocioVioladaException {

    public AreaNoRecibeLlamadasException(String areaNombre) {
        super("El área %s no recibe llamadas, no puede crear ni resolver tickets de origen telefónico".formatted(areaNombre));
    }
}

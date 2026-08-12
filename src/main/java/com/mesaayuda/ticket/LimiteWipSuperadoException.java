package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class LimiteWipSuperadoException extends ReglaNegocioVioladaException {

    public LimiteWipSuperadoException(long enProgreso, int limite) {
        super("El agente ya tiene %d ticket(s) en progreso, el límite de su área es %d".formatted(enProgreso, limite));
    }
}

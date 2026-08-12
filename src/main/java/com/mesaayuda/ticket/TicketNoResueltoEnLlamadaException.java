package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class TicketNoResueltoEnLlamadaException extends ReglaNegocioVioladaException {

    public TicketNoResueltoEnLlamadaException() {
        super("Solo un ticket marcado como resuelto en la llamada puede pasar directo de NUEVO a RESUELTO");
    }
}

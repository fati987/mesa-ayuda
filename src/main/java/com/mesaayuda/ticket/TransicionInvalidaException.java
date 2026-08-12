package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;
import com.mesaayuda.ticket.enums.EstadoTicket;

public class TransicionInvalidaException extends ReglaNegocioVioladaException {

    public TransicionInvalidaException(EstadoTicket origen, EstadoTicket destino) {
        super("No se puede pasar un ticket de %s a %s".formatted(origen, destino));
    }
}

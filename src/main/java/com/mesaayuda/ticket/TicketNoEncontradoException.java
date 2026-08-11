package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class TicketNoEncontradoException extends RecursoNoEncontradoException {

    public TicketNoEncontradoException(String codigo) {
        super("No se encontró el ticket con código %s".formatted(codigo));
    }
}

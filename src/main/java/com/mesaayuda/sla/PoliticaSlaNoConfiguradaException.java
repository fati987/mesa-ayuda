package com.mesaayuda.sla;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;
import com.mesaayuda.ticket.enums.Prioridad;

public class PoliticaSlaNoConfiguradaException extends RecursoNoEncontradoException {

    public PoliticaSlaNoConfiguradaException(Prioridad prioridad) {
        super("No hay ninguna política de SLA activa configurada para la prioridad %s".formatted(prioridad));
    }
}

package com.mesaayuda.sla;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class CalendarioLaboralNoConfiguradoException extends RecursoNoEncontradoException {

    public CalendarioLaboralNoConfiguradoException() {
        super("No hay ningún calendario laboral activo configurado");
    }
}

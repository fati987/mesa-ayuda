package com.mesaayuda.ticket;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class AgenteFueraDeAreaException extends ReglaNegocioVioladaException {

    public AgenteFueraDeAreaException() {
        super("Solo un agente del área destino puede tomar este ticket");
    }
}

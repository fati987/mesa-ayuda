package com.mesaayuda.usuario;

import com.mesaayuda.shared.excepcion.ReglaNegocioVioladaException;

public class CorreoYaRegistradoException extends ReglaNegocioVioladaException {

    public CorreoYaRegistradoException(String correo) {
        super("Ya existe un usuario registrado con el correo %s".formatted(correo));
    }
}

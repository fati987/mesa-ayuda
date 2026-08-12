package com.mesaayuda.usuario;

import com.mesaayuda.shared.excepcion.RecursoNoEncontradoException;

public class UsuarioNoEncontradoException extends RecursoNoEncontradoException {

    public UsuarioNoEncontradoException(String correo) {
        super("No se encontró el usuario con correo %s".formatted(correo));
    }
}

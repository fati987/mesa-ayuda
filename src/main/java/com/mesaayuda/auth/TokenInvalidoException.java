package com.mesaayuda.auth;

import com.mesaayuda.shared.excepcion.AutenticacionFallidaException;

public class TokenInvalidoException extends AutenticacionFallidaException {

    public TokenInvalidoException() {
        super("El token de actualización es inválido, expiró o el usuario ya no está activo");
    }
}

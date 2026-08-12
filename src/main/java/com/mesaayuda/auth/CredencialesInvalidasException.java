package com.mesaayuda.auth;

import com.mesaayuda.shared.excepcion.AutenticacionFallidaException;

public class CredencialesInvalidasException extends AutenticacionFallidaException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña inválidos");
    }
}

package com.mesaayuda.sla;

/**
 * Guarda defensiva: un CalendarioLaboral sin ningún día hábil marcado
 * haría que la búsqueda del próximo día hábil no termine nunca. No
 * debería darse con los datos semilla; si ocurre, es un error de
 * configuración, no una regla de negocio del consumidor de la API.
 */
public class CalendarioLaboralSinDiasHabilesException extends RuntimeException {

    public CalendarioLaboralSinDiasHabilesException() {
        super("El calendario laboral no tiene ningún día hábil configurado");
    }
}

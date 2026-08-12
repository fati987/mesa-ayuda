package com.mesaayuda.ticket;

import com.mesaayuda.area.Area;
import com.mesaayuda.auth.UsuarioPrincipal;

/**
 * Vehículo de los datos que las guardas de TransicionService necesitan y
 * que no viven ya en el propio Ticket: quién ejecuta la transición, y los
 * datos específicos de cada tipo de transición (derivación / resolución).
 */
public record ContextoTransicion(
        UsuarioPrincipal ejecutor,
        Area areaDestino,
        String motivoDerivacion,
        String solucion,
        String comentario) {

    public static ContextoTransicion simple(UsuarioPrincipal ejecutor) {
        return new ContextoTransicion(ejecutor, null, null, null, null);
    }

    public static ContextoTransicion simple(UsuarioPrincipal ejecutor, String comentario) {
        return new ContextoTransicion(ejecutor, null, null, null, comentario);
    }

    public static ContextoTransicion derivacion(UsuarioPrincipal ejecutor, Area areaDestino, String motivo) {
        return new ContextoTransicion(ejecutor, areaDestino, motivo, null, null);
    }

    public static ContextoTransicion resolucion(UsuarioPrincipal ejecutor, String solucion) {
        return new ContextoTransicion(ejecutor, null, null, solucion, null);
    }
}

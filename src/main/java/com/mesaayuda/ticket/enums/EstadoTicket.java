package com.mesaayuda.ticket.enums;

import java.util.Map;
import java.util.Set;

/**
 * El grafo de transiciones vive acá (regla de negocio del CLAUDE.md); las
 * guardas que consultan base de datos viven en TransicionService.
 *
 * El mapa no puede construirse en el constructor del enum porque las
 * constantes todavía no existen en ese punto — se puebla en un campo
 * static final después de declarados los 6 valores.
 *
 * CERRADO -> EN_PROGRESO (reapertura) se acepta estructuralmente sin guarda
 * de "ventana de tiempo": no hay valor de N días definido todavía ni
 * concepto de calendario laboral (eso es Sprint 4). Comportamiento temporal
 * documentado, no definitivo.
 */
public enum EstadoTicket {
    NUEVO,
    DERIVADO,
    EN_PROGRESO,
    ESPERANDO_CLIENTE,
    RESUELTO,
    CERRADO;

    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES = Map.of(
            NUEVO, Set.of(RESUELTO, DERIVADO),
            DERIVADO, Set.of(EN_PROGRESO, DERIVADO),
            EN_PROGRESO, Set.of(ESPERANDO_CLIENTE, RESUELTO, DERIVADO),
            ESPERANDO_CLIENTE, Set.of(EN_PROGRESO),
            RESUELTO, Set.of(CERRADO),
            CERRADO, Set.of(EN_PROGRESO));

    public boolean puedeTransicionarA(EstadoTicket destino) {
        return TRANSICIONES.getOrDefault(this, Set.of()).contains(destino);
    }

    public Set<EstadoTicket> destinosPermitidos() {
        return TRANSICIONES.getOrDefault(this, Set.of());
    }
}

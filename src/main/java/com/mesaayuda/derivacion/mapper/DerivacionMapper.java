package com.mesaayuda.derivacion.mapper;

import com.mesaayuda.derivacion.Derivacion;
import com.mesaayuda.derivacion.dto.DerivacionDto;

/**
 * Mapeo manual: accede a relaciones lazy (areaOrigen, areaDestino,
 * usuarioDeriva), debe invocarse dentro de una transacción de lectura
 * abierta (ver TicketService).
 */
public final class DerivacionMapper {

    private DerivacionMapper() {
    }

    public static DerivacionDto aDto(Derivacion derivacion) {
        return new DerivacionDto(
                derivacion.getMotivo(),
                derivacion.getAreaOrigen().getNombre(),
                derivacion.getAreaDestino().getNombre(),
                derivacion.getUsuarioDeriva().getNombreCompleto(),
                derivacion.getCreadoEn());
    }
}

package com.mesaayuda.derivacion.dto;

import java.time.Instant;

public record DerivacionDto(
        String motivo,
        String areaOrigenNombre,
        String areaDestinoNombre,
        String usuarioDerivaNombre,
        Instant creadoEn) {
}

package com.mesaayuda.llamada.dto;

import java.time.Instant;

public record LlamadaResumenDto(
        Instant fechaHora,
        Integer duracionSegundos,
        String usuarioAtiendeNombre) {
}

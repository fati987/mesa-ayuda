package com.mesaayuda.metricas.dto;

import java.time.LocalDate;
import java.util.List;

public record FlujoAcumuladoDto(
        LocalDate desde,
        LocalDate hasta,
        List<FlujoAcumuladoPuntoDto> puntos) {
}

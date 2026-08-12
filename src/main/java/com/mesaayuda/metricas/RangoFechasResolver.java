package com.mesaayuda.metricas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

/**
 * Convierte un rango de LocalDate (tal como llega por query param) en un
 * intervalo de instantes [desde, hasta) en la zona horaria del calendario
 * laboral activo, o valida un rango de días con un tope máximo (para el
 * diagrama de flujo acumulado, que trabaja por día calendario, no por
 * instante).
 */
@Component
public class RangoFechasResolver {

    public record RangoInstantes(Instant desde, Instant hasta) {
    }

    public RangoInstantes resolver(LocalDate desde, LocalDate hasta, ZoneId zona) {
        validarOrden(desde, hasta);
        Instant desdeInstant = desde.atStartOfDay(zona).toInstant();
        Instant hastaInstant = hasta.atStartOfDay(zona).toInstant();
        return new RangoInstantes(desdeInstant, hastaInstant);
    }

    public void validarRangoMaximoDias(LocalDate desde, LocalDate hasta, int maxDias) {
        validarOrden(desde, hasta);
        long dias = ChronoUnit.DAYS.between(desde, hasta);
        if (dias > maxDias) {
            throw new RangoFechasInvalidoException("El rango no puede superar los %d días".formatted(maxDias));
        }
    }

    private void validarOrden(LocalDate desde, LocalDate hasta) {
        if (!hasta.isAfter(desde)) {
            throw new RangoFechasInvalidoException("La fecha 'hasta' debe ser posterior a 'desde'");
        }
    }
}

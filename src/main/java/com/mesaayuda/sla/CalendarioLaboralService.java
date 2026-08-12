package com.mesaayuda.sla;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Algoritmos puros de horario hábil: no acceden a base de datos, reciben
 * el CalendarioLaboral y sus feriados por parámetro. Ventana hábil
 * inclusiva al inicio, exclusiva al fin ([horaInicio, horaFin)) — un
 * instante exactamente a la hora de cierre cuenta como fuera de jornada.
 */
@Component
public class CalendarioLaboralService {

    private static final int TOPE_ITERACIONES_DIAS = 3650;

    public Instant calcularVencimiento(Instant inicio, int minutosSla, CalendarioLaboral calendario, Set<LocalDate> feriados) {
        ZonedDateTime cursor = alinearAInicioHabil(inicio.atZone(calendario.zoneId()), calendario, feriados);
        long restante = minutosSla;

        for (int i = 0; i < TOPE_ITERACIONES_DIAS; i++) {
            ZonedDateTime finJornada = cursor.toLocalDate().atTime(calendario.getHoraFin()).atZone(calendario.zoneId());
            long disponibleHoy = Duration.between(cursor, finJornada).toMinutes();
            if (restante <= disponibleHoy) {
                return cursor.plusMinutes(restante).toInstant();
            }
            restante -= disponibleHoy;
            LocalDate siguiente = siguienteDiaHabil(cursor.toLocalDate(), calendario, feriados);
            cursor = siguiente.atTime(calendario.getHoraInicio()).atZone(calendario.zoneId());
        }
        throw new CalendarioLaboralSinDiasHabilesException();
    }

    public long minutosHabilesEntre(Instant inicio, Instant fin, CalendarioLaboral calendario, Set<LocalDate> feriados) {
        if (!fin.isAfter(inicio)) {
            return 0;
        }
        ZoneId zona = calendario.zoneId();
        ZonedDateTime zInicio = inicio.atZone(zona);
        ZonedDateTime zFin = fin.atZone(zona);

        long total = 0;
        LocalDate dia = zInicio.toLocalDate();
        LocalDate ultimoDia = zFin.toLocalDate();
        while (!dia.isAfter(ultimoDia)) {
            if (esDiaHabil(dia, calendario, feriados)) {
                ZonedDateTime inicioJornada = dia.atTime(calendario.getHoraInicio()).atZone(zona);
                ZonedDateTime finJornada = dia.atTime(calendario.getHoraFin()).atZone(zona);
                ZonedDateTime ventanaInicio = maxZdt(inicioJornada, zInicio);
                ZonedDateTime ventanaFin = minZdt(finJornada, zFin);
                if (ventanaFin.isAfter(ventanaInicio)) {
                    total += Duration.between(ventanaInicio, ventanaFin).toMinutes();
                }
            }
            dia = dia.plusDays(1);
        }
        return total;
    }

    private ZonedDateTime alinearAInicioHabil(ZonedDateTime punto, CalendarioLaboral calendario, Set<LocalDate> feriados) {
        LocalDate dia = punto.toLocalDate();
        LocalTime horaInicio = calendario.getHoraInicio();
        LocalTime horaFin = calendario.getHoraFin();

        if (esDiaHabil(dia, calendario, feriados)) {
            LocalTime hora = punto.toLocalTime();
            if (hora.isBefore(horaInicio)) {
                return dia.atTime(horaInicio).atZone(punto.getZone());
            }
            if (hora.isBefore(horaFin)) {
                return punto;
            }
        }
        LocalDate siguiente = siguienteDiaHabil(dia, calendario, feriados);
        return siguiente.atTime(horaInicio).atZone(punto.getZone());
    }

    private LocalDate siguienteDiaHabil(LocalDate desde, CalendarioLaboral calendario, Set<LocalDate> feriados) {
        LocalDate candidato = desde.plusDays(1);
        for (int i = 0; i < TOPE_ITERACIONES_DIAS; i++) {
            if (esDiaHabil(candidato, calendario, feriados)) {
                return candidato;
            }
            candidato = candidato.plusDays(1);
        }
        throw new CalendarioLaboralSinDiasHabilesException();
    }

    private boolean esDiaHabil(LocalDate dia, CalendarioLaboral calendario, Set<LocalDate> feriados) {
        return calendario.aplicaA(dia.getDayOfWeek()) && !feriados.contains(dia);
    }

    private static ZonedDateTime maxZdt(ZonedDateTime a, ZonedDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static ZonedDateTime minZdt(ZonedDateTime a, ZonedDateTime b) {
        return a.isBefore(b) ? a : b;
    }
}

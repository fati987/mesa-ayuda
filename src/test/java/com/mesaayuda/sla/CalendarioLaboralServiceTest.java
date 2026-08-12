package com.mesaayuda.sla;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Casos borde exigidos por CLAUDE.md: viernes por la tarde, fin de semana,
 * feriado, cambio de día y reloj pausado. Las fechas de referencia se
 * derivan con TemporalAdjusters desde un ancla fija, nunca de la fecha
 * real del sistema — así el test es determinístico sin importar qué día
 * se ejecute.
 */
class CalendarioLaboralServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");
    private static final LocalDate VIERNES = LocalDate.of(2026, 1, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    private static final LocalDate SABADO = VIERNES.plusDays(1);
    private static final LocalDate DOMINGO = VIERNES.plusDays(2);
    private static final LocalDate LUNES = VIERNES.plusDays(3);
    private static final LocalDate MARTES = VIERNES.plusDays(4);
    private static final LocalDate MIERCOLES = VIERNES.plusDays(5);
    private static final LocalDate JUEVES = VIERNES.plusDays(6);

    private final CalendarioLaboralService service = new CalendarioLaboralService();

    // =====================================================================
    // calcularVencimiento
    // =====================================================================

    @Test
    void criterioDeTermino_viernesA18HorasConSlaDe4Horas_venceLunesA13() {
        Instant inicio = instante(VIERNES, LocalTime.of(18, 0));

        Instant vencimiento = service.calcularVencimiento(inicio, 240, calendarioChile(), Set.of());

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(LUNES, LocalTime.of(13, 0)));
    }

    @Test
    void inicioEnFinDeSemana_arrancaLunesAlInicioDeJornada() {
        Instant inicio = instante(SABADO, LocalTime.of(10, 0));

        Instant vencimiento = service.calcularVencimiento(inicio, 30, calendarioChile(), Set.of());

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(LUNES, LocalTime.of(9, 30)));
    }

    @Test
    void feriadoEntreMedio_seSaltaCompleto() {
        Instant inicio = instante(MARTES, LocalTime.of(10, 0));

        // 480 min agotan el resto del martes (10:00-18:00); el miércoles es
        // feriado y se salta entero; los 60 min restantes caen el jueves.
        Instant vencimiento = service.calcularVencimiento(inicio, 540, calendarioChile(), Set.of(MIERCOLES));

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(JUEVES, LocalTime.of(10, 0)));
    }

    @Test
    void cambioDeDia_sinFeriadoNiFinDeSemana() {
        Instant inicio = instante(MARTES, LocalTime.of(17, 0));

        Instant vencimiento = service.calcularVencimiento(inicio, 120, calendarioChile(), Set.of());

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(MIERCOLES, LocalTime.of(10, 0)));
    }

    @Test
    void inicioAntesDeJornada_noSaltaAlDiaSiguiente() {
        Instant inicio = instante(LUNES, LocalTime.of(7, 0));

        Instant vencimiento = service.calcularVencimiento(inicio, 60, calendarioChile(), Set.of());

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(LUNES, LocalTime.of(10, 0)));
    }

    @Test
    void inicioNocturnoEntreSemana() {
        Instant inicio = instante(MARTES, LocalTime.of(22, 0));

        Instant vencimiento = service.calcularVencimiento(inicio, 30, calendarioChile(), Set.of());

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(MIERCOLES, LocalTime.of(9, 30)));
    }

    @Test
    void bordeExacto_jornadaCompletaVenceAlCierre() {
        Instant inicio = instante(LUNES, LocalTime.of(9, 0));

        Instant vencimiento = service.calcularVencimiento(inicio, 540, calendarioChile(), Set.of());

        assertThat(santiago(vencimiento)).isEqualTo(LocalDateTime.of(LUNES, LocalTime.of(18, 0)));
    }

    // =====================================================================
    // minutosHabilesEntre (reloj pausado)
    // =====================================================================

    @Test
    void pausaIntradia() {
        Instant inicio = instante(MARTES, LocalTime.of(10, 0));
        Instant fin = instante(MARTES, LocalTime.of(10, 45));

        assertThat(service.minutosHabilesEntre(inicio, fin, calendarioChile(), Set.of())).isEqualTo(45);
    }

    @Test
    void pausaQueCruzaFinDeSemana() {
        Instant inicio = instante(VIERNES, LocalTime.of(17, 30));
        Instant fin = instante(LUNES, LocalTime.of(9, 15));

        assertThat(service.minutosHabilesEntre(inicio, fin, calendarioChile(), Set.of())).isEqualTo(45);
    }

    @Test
    void pausaEnteramenteEnFinDeSemana() {
        Instant inicio = instante(SABADO, LocalTime.of(10, 0));
        Instant fin = instante(DOMINGO, LocalTime.of(20, 0));

        assertThat(service.minutosHabilesEntre(inicio, fin, calendarioChile(), Set.of())).isEqualTo(0);
    }

    @Test
    void pausaConFeriadoEnMedio() {
        Instant inicio = instante(LUNES, LocalTime.of(17, 0));
        Instant fin = instante(MIERCOLES, LocalTime.of(9, 30));

        long minutos = service.minutosHabilesEntre(inicio, fin, calendarioChile(), Set.of(MARTES));

        assertThat(minutos).isEqualTo(90);
    }

    @Test
    void finNoPosteriorAInicio_devuelveCero() {
        Instant inicio = instante(MARTES, LocalTime.of(10, 0));
        Instant fin = instante(MARTES, LocalTime.of(9, 0));

        assertThat(service.minutosHabilesEntre(inicio, fin, calendarioChile(), Set.of())).isEqualTo(0);
    }

    // =====================================================================
    // Fixtures
    // =====================================================================

    private static CalendarioLaboral calendarioChile() {
        CalendarioLaboral calendario = new CalendarioLaboral();
        calendario.setNombre("Calendario Chile Continental");
        calendario.setZonaHoraria(ZONA.getId());
        calendario.setHoraInicio(LocalTime.of(9, 0));
        calendario.setHoraFin(LocalTime.of(18, 0));
        calendario.setAplicaLunes(true);
        calendario.setAplicaMartes(true);
        calendario.setAplicaMiercoles(true);
        calendario.setAplicaJueves(true);
        calendario.setAplicaViernes(true);
        calendario.setAplicaSabado(false);
        calendario.setAplicaDomingo(false);
        calendario.setActivo(true);
        return calendario;
    }

    private static Instant instante(LocalDate fecha, LocalTime hora) {
        return ZonedDateTime.of(fecha, hora, ZONA).toInstant();
    }

    private static LocalDateTime santiago(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZONA);
    }
}

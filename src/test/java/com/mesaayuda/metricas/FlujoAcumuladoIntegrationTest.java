package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.mesaayuda.metricas.dto.FlujoAcumuladoDto;
import com.mesaayuda.metricas.dto.FlujoAcumuladoPuntoDto;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class FlujoAcumuladoIntegrationTest {

    @Autowired
    private FlujoAcumuladoService flujoAcumuladoService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        // Rango acotado (no los +/-5 años de otras métricas): flujoAcumulado
        // rechaza rangos de más de 366 días.
        LocalDate desde = LocalDate.now(clock).minusDays(200);
        LocalDate hasta = LocalDate.now(clock).plusDays(1);

        FlujoAcumuladoDto dto = flujoAcumuladoService.calcular(desde, hasta);

        assertThat(dto.puntos()).isNotNull();
        dto.puntos().forEach(p -> {
            assertThat(p.cantidad()).isGreaterThan(0);
            assertThat(p.estado()).isNotNull();
            assertThat(p.dia()).isBetween(desde, hasta);
        });

        // Invariante: los tickets no desaparecen, así que el total de
        // tickets vivos (suma de cantidad por estado) en un día no puede
        // ser menor que en un día anterior dentro del rango.
        Map<LocalDate, Long> totalPorDia = new TreeMap<>();
        for (FlujoAcumuladoPuntoDto punto : dto.puntos()) {
            totalPorDia.merge(punto.dia(), punto.cantidad(), Long::sum);
        }
        long anterior = 0;
        for (long total : totalPorDia.values()) {
            assertThat(total).isGreaterThanOrEqualTo(anterior);
            anterior = total;
        }
    }

    @Test
    void rechazaUnRangoDeMasDe366Dias() {
        LocalDate desde = LocalDate.now(clock).minusDays(400);
        LocalDate hasta = LocalDate.now(clock);

        assertThatThrownBy(() -> flujoAcumuladoService.calcular(desde, hasta))
                .isInstanceOf(RangoFechasInvalidoException.class);
    }
}

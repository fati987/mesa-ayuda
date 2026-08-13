package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.mesaayuda.metricas.dto.CumplimientoPorDimensionDto;
import com.mesaayuda.metricas.dto.CumplimientoSlaDto;
import com.mesaayuda.testsoporte.PostgresTestcontainer;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class CumplimientoSlaIntegrationTest extends PostgresTestcontainer {

    @Autowired
    private CumplimientoSlaService cumplimientoSlaService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        LocalDate desde = LocalDate.now(clock).minusYears(5);
        LocalDate hasta = LocalDate.now(clock).plusYears(1);

        CumplimientoSlaDto dto = cumplimientoSlaService.calcular(desde, hasta);

        assertThat(dto.total()).isEqualTo(dto.cumplidos() + dto.incumplidos() + dto.enCurso());
        assertPorcentajeValido(dto.porcentajeCumplimiento());

        assertDesgloseCoherente(dto.porCategoria(), dto.total());
        assertDesgloseCoherente(dto.porArea(), dto.total());
        assertDesgloseCoherente(dto.porPrioridad(), dto.total());
    }

    private void assertDesgloseCoherente(List<CumplimientoPorDimensionDto> desglose, long total) {
        long suma = desglose.stream().mapToLong(CumplimientoPorDimensionDto::total).sum();
        assertThat(suma).isEqualTo(total);
        desglose.forEach(d -> {
            assertThat(d.nombre()).isNotBlank();
            assertThat(d.total()).isEqualTo(d.cumplidos() + d.incumplidos() + d.enCurso());
            assertPorcentajeValido(d.porcentajeCumplimiento());
        });
    }

    private void assertPorcentajeValido(BigDecimal porcentaje) {
        if (porcentaje != null) {
            assertThat(porcentaje.doubleValue()).isBetween(0.0, 100.0);
        }
    }
}

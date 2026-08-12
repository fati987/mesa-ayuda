package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.mesaayuda.metricas.dto.FcrPorAreaDto;
import com.mesaayuda.metricas.dto.FcrPorDiaDto;
import com.mesaayuda.metricas.dto.FcrResumenDto;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class FcrIntegrationTest {

    @Autowired
    private FcrService fcrService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        LocalDate desde = LocalDate.now(clock).minusYears(5);
        LocalDate hasta = LocalDate.now(clock).plusYears(1);

        FcrResumenDto resumen = fcrService.calcular(desde, hasta);

        assertThat(resumen.totalCreados()).isGreaterThan(0);
        assertThat(resumen.resueltosEnLlamada()).isBetween(0L, resumen.totalCreados());
        assertPorcentajeValido(resumen.porcentajeFcr());

        long sumaCreadosPorArea = resumen.porArea().stream().mapToLong(FcrPorAreaDto::totalCreados).sum();
        assertThat(sumaCreadosPorArea).isEqualTo(resumen.totalCreados());
        long sumaResueltosPorArea = resumen.porArea().stream().mapToLong(FcrPorAreaDto::resueltosEnLlamada).sum();
        assertThat(sumaResueltosPorArea).isEqualTo(resumen.resueltosEnLlamada());

        resumen.porArea().forEach(a -> {
            assertThat(a.areaId()).isNotNull();
            assertThat(a.areaNombre()).isNotBlank();
            assertThat(a.resueltosEnLlamada()).isBetween(0L, a.totalCreados());
            assertPorcentajeValido(a.porcentajeFcr());
        });

        long sumaCreadosPorDia = resumen.porDia().stream().mapToLong(FcrPorDiaDto::totalCreados).sum();
        assertThat(sumaCreadosPorDia).isEqualTo(resumen.totalCreados());
        resumen.porDia().forEach(d -> assertPorcentajeValido(d.porcentajeFcr()));
    }

    private void assertPorcentajeValido(BigDecimal porcentaje) {
        if (porcentaje != null) {
            assertThat(porcentaje.doubleValue()).isBetween(0.0, 100.0);
        }
    }
}

package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.mesaayuda.metricas.dto.TiempoEsperaPorAreaDto;
import com.mesaayuda.metricas.dto.TiempoEsperaResumenDto;
import com.mesaayuda.testsoporte.PostgresTestcontainer;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class TiempoEsperaIntegrationTest extends PostgresTestcontainer {

    @Autowired
    private TiempoEsperaService tiempoEsperaService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        LocalDate desde = LocalDate.now(clock).minusYears(5);
        LocalDate hasta = LocalDate.now(clock).plusYears(1);

        TiempoEsperaResumenDto resumen = tiempoEsperaService.calcular(desde, hasta);

        assertThat(resumen.tomasConsideradas()).isGreaterThanOrEqualTo(0);
        assertNoNegativo(resumen.esperaPromedioMinutos());
        assertNoNegativo(resumen.esperaMedianaMinutos());

        if (resumen.tomasConsideradas() == 0) {
            assertThat(resumen.esperaPromedioMinutos()).isNull();
            assertThat(resumen.esperaMedianaMinutos()).isNull();
            assertThat(resumen.porArea()).isEmpty();
        } else {
            long sumaTomasPorArea = resumen.porArea().stream().mapToLong(TiempoEsperaPorAreaDto::tomasConsideradas).sum();
            assertThat(sumaTomasPorArea).isLessThanOrEqualTo(resumen.tomasConsideradas());
            resumen.porArea().forEach(a -> {
                assertThat(a.areaNombre()).isNotBlank();
                assertNoNegativo(a.esperaPromedioMinutos());
            });
        }
    }

    private void assertNoNegativo(BigDecimal valor) {
        if (valor != null) {
            assertThat(valor.doubleValue()).isGreaterThanOrEqualTo(0.0);
        }
    }
}

package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.mesaayuda.metricas.dto.TiempoCicloResumenDto;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class TiempoCicloIntegrationTest {

    @Autowired
    private TiempoCicloService tiempoCicloService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        LocalDate desde = LocalDate.now(clock).minusYears(5);
        LocalDate hasta = LocalDate.now(clock).plusYears(1);

        TiempoCicloResumenDto resumen = tiempoCicloService.calcular(desde, hasta);

        assertThat(resumen.ticketsLeadTime()).isGreaterThanOrEqualTo(0);
        assertThat(resumen.ticketsCycleTime()).isGreaterThanOrEqualTo(0);
        // cycle time excluye tickets resueltos en llamada (nunca pasan por
        // EN_PROGRESO), así que su universo nunca puede ser mayor que el de lead time.
        assertThat(resumen.ticketsCycleTime()).isLessThanOrEqualTo(resumen.ticketsLeadTime());

        assertNoNegativo(resumen.leadTimePromedioMinutos());
        assertNoNegativo(resumen.leadTimeMedianoMinutos());
        assertNoNegativo(resumen.cycleTimePromedioMinutos());
        assertNoNegativo(resumen.cycleTimeMedianoMinutos());

        if (resumen.ticketsLeadTime() == 0) {
            assertThat(resumen.leadTimePromedioMinutos()).isNull();
            assertThat(resumen.leadTimeMedianoMinutos()).isNull();
        }
        if (resumen.ticketsCycleTime() == 0) {
            assertThat(resumen.cycleTimePromedioMinutos()).isNull();
            assertThat(resumen.cycleTimeMedianoMinutos()).isNull();
        }
    }

    private void assertNoNegativo(BigDecimal valor) {
        if (valor != null) {
            assertThat(valor.doubleValue()).isGreaterThanOrEqualTo(0.0);
        }
    }
}

package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.PageRequest;

import com.mesaayuda.metricas.dto.LlamadaMetricasResumenDto;
import com.mesaayuda.metricas.dto.LlamadaMultiTicketDto;
import com.mesaayuda.shared.paginacion.PaginaResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class LlamadaMetricasIntegrationTest {

    @Autowired
    private LlamadaMetricasService llamadaMetricasService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        LocalDate desde = LocalDate.now(clock).minusYears(5);
        LocalDate hasta = LocalDate.now(clock).plusYears(1);

        LlamadaMetricasResumenDto resumen = llamadaMetricasService.calcular(desde, hasta);

        assertThat(resumen.totalLlamadas()).isGreaterThan(0);
        assertThat(resumen.llamadasConMultiplesTickets()).isBetween(0L, resumen.totalLlamadas());
        assertPorcentajeValido(resumen.porcentajeMultiplesTickets());
        if (resumen.duracionPromedioSegundos() != null) {
            assertThat(resumen.duracionPromedioSegundos().doubleValue()).isGreaterThanOrEqualTo(0.0);
        }

        PaginaResponse<LlamadaMultiTicketDto> pagina = llamadaMetricasService.llamadasConMultiplesTickets(desde, hasta, PageRequest.of(0, 20));
        assertThat(pagina.totalElementos()).isEqualTo(resumen.llamadasConMultiplesTickets());
        pagina.content().forEach(l -> {
            assertThat(l.llamadaId()).isNotNull();
            assertThat(l.contactoNombre()).isNotBlank();
            assertThat(l.cantidadTickets()).isGreaterThan(1);
        });
    }

    private void assertPorcentajeValido(BigDecimal porcentaje) {
        if (porcentaje != null) {
            assertThat(porcentaje.doubleValue()).isBetween(0.0, 100.0);
        }
    }
}

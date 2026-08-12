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

import com.mesaayuda.metricas.dto.TasaDerivacionPorAreaDto;
import com.mesaayuda.metricas.dto.TasaDerivacionResumenDto;
import com.mesaayuda.metricas.dto.TicketRebotadoDto;
import com.mesaayuda.shared.paginacion.PaginaResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class TasaDerivacionIntegrationTest {

    @Autowired
    private TasaDerivacionService tasaDerivacionService;
    @Autowired
    private Clock clock;

    @Test
    void devuelveDatosCoherentesSobreElConjuntoSemilla() {
        LocalDate desde = LocalDate.now(clock).minusYears(5);
        LocalDate hasta = LocalDate.now(clock).plusYears(1);

        TasaDerivacionResumenDto resumen = tasaDerivacionService.calcular(desde, hasta);

        assertThat(resumen.totalCreados()).isGreaterThan(0);
        assertThat(resumen.derivadosAlMenosUnaVez()).isBetween(0L, resumen.totalCreados());
        assertThat(resumen.ticketsRebotados()).isBetween(0L, resumen.derivadosAlMenosUnaVez());
        assertPorcentajeValido(resumen.tasaDerivacion());
        assertPorcentajeValido(resumen.tasaRebote());

        long sumaCreadosPorArea = resumen.porArea().stream().mapToLong(TasaDerivacionPorAreaDto::totalCreados).sum();
        assertThat(sumaCreadosPorArea).isEqualTo(resumen.totalCreados());
        long sumaDerivadosPorArea = resumen.porArea().stream().mapToLong(TasaDerivacionPorAreaDto::derivadosAlMenosUnaVez).sum();
        assertThat(sumaDerivadosPorArea).isEqualTo(resumen.derivadosAlMenosUnaVez());

        resumen.porArea().forEach(a -> assertPorcentajeValido(a.tasaDerivacion()));

        PaginaResponse<TicketRebotadoDto> pagina = tasaDerivacionService.rebotes(desde, hasta, PageRequest.of(0, 20));
        assertThat(pagina.totalElementos()).isEqualTo(resumen.ticketsRebotados());
        pagina.content().forEach(t -> {
            assertThat(t.codigo()).isNotBlank();
            assertThat(t.cantidadDerivaciones()).isGreaterThan(1);
        });
    }

    private void assertPorcentajeValido(BigDecimal porcentaje) {
        if (porcentaje != null) {
            assertThat(porcentaje.doubleValue()).isBetween(0.0, 100.0);
        }
    }
}

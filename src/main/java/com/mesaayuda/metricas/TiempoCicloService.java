package com.mesaayuda.metricas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.metricas.MetricasTiempoHabilCalculator.Pausa;
import com.mesaayuda.metricas.RangoFechasResolver.RangoInstantes;
import com.mesaayuda.metricas.dto.TiempoCicloResumenDto;
import com.mesaayuda.metricas.proyeccion.HistorialTransicionProjection;
import com.mesaayuda.sla.VencimientoService;
import com.mesaayuda.ticket.enums.EstadoTicket;

/**
 * Lead time (NUEVO -> primera RESUELTO) y cycle time (primera EN_PROGRESO
 * -> primera RESUELTO, excluye tickets resueltos en llamada, que nunca
 * pasan por EN_PROGRESO), recalculados desde HistorialEstado en horario
 * hábil — no restando ticket.minutosPausado directo, que es un acumulado
 * de por vida que se contaminaría si el ticket se reabre después de su
 * primera resolución.
 */
@Service
@Transactional(readOnly = true)
public class TiempoCicloService {

    private final MetricasTicketRepository metricasTicketRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;
    private final MetricasTiempoHabilCalculator calculator;

    public TiempoCicloService(MetricasTicketRepository metricasTicketRepository, RangoFechasResolver rangoFechasResolver,
            VencimientoService vencimientoService, MetricasTiempoHabilCalculator calculator) {
        this.metricasTicketRepository = metricasTicketRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
        this.calculator = calculator;
    }

    private record ResultadoTicket(long leadTimeMinutos, Long cycleTimeMinutos) {
    }

    public TiempoCicloResumenDto calcular(LocalDate desde, LocalDate hasta) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);

        List<HistorialTransicionProjection> filas = metricasTicketRepository.historialParaTicketsResueltosEn(rango.desde(), rango.hasta());

        List<ResultadoTicket> resultados = filas.stream()
                .collect(Collectors.groupingBy(HistorialTransicionProjection::getTicketId, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(this::procesarTicket)
                .flatMap(Optional::stream)
                .toList();

        List<Long> leadTimes = resultados.stream().map(ResultadoTicket::leadTimeMinutos).toList();
        List<Long> cycleTimes = resultados.stream().map(ResultadoTicket::cycleTimeMinutos).filter(java.util.Objects::nonNull).toList();

        return new TiempoCicloResumenDto(desde, hasta,
                leadTimes.size(), EstadisticasMinutosCalculator.promedio(leadTimes), EstadisticasMinutosCalculator.mediana(leadTimes),
                cycleTimes.size(), EstadisticasMinutosCalculator.promedio(cycleTimes), EstadisticasMinutosCalculator.mediana(cycleTimes));
    }

    private Optional<ResultadoTicket> procesarTicket(List<HistorialTransicionProjection> filas) {
        int idxResuelto = -1;
        for (int i = 0; i < filas.size(); i++) {
            if (filas.get(i).getEstadoNuevo() == EstadoTicket.RESUELTO) {
                idxResuelto = i;
                break;
            }
        }
        if (idxResuelto < 0) {
            return Optional.empty();
        }

        List<HistorialTransicionProjection> relevantes = filas.subList(0, idxResuelto + 1);
        Instant inicioLead = relevantes.get(0).getCreadoEn();
        Instant finResolucion = relevantes.get(idxResuelto).getCreadoEn();

        List<Pausa> pausas = new ArrayList<>();
        Instant inicioCycle = null;
        for (int i = 0; i < relevantes.size(); i++) {
            HistorialTransicionProjection fila = relevantes.get(i);
            if (inicioCycle == null && fila.getEstadoNuevo() == EstadoTicket.EN_PROGRESO) {
                inicioCycle = fila.getCreadoEn();
            }
            if (i > 0 && fila.getEstadoAnterior() == EstadoTicket.ESPERANDO_CLIENTE && fila.getEstadoNuevo() == EstadoTicket.EN_PROGRESO) {
                pausas.add(new Pausa(relevantes.get(i - 1).getCreadoEn(), fila.getCreadoEn()));
            }
        }

        long leadTimeMinutos = calculator.minutosHabilesDescontandoPausas(inicioLead, finResolucion, pausas);
        Long cycleTimeMinutos = inicioCycle != null
                ? calculator.minutosHabilesDescontandoPausas(inicioCycle, finResolucion, pausas)
                : null;

        return Optional.of(new ResultadoTicket(leadTimeMinutos, cycleTimeMinutos));
    }
}

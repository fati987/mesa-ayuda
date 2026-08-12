package com.mesaayuda.metricas;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.metricas.RangoFechasResolver.RangoInstantes;
import com.mesaayuda.metricas.dto.TiempoEsperaPorAreaDto;
import com.mesaayuda.metricas.dto.TiempoEsperaResumenDto;
import com.mesaayuda.metricas.proyeccion.DerivacionOrdinalProjection;
import com.mesaayuda.metricas.proyeccion.HistorialTransicionProjection;
import com.mesaayuda.sla.VencimientoService;
import com.mesaayuda.ticket.enums.EstadoTicket;

/**
 * Tiempo entre la entrada a DERIVADO y la toma real (DERIVADO->EN_PROGRESO)
 * siguiente, en minutos hábiles. El área destino de cada toma se
 * correlaciona por posición ordinal con Derivacion: la N-ésima entrada a
 * DERIVADO de un ticket = la N-ésima fila de Derivacion de ese ticket,
 * ambas insertadas en la misma transacción de TransicionService.aplicarDerivacion.
 */
@Service
@Transactional(readOnly = true)
public class TiempoEsperaService {

    private final MetricasTicketRepository metricasTicketRepository;
    private final AreaRepository areaRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;

    public TiempoEsperaService(MetricasTicketRepository metricasTicketRepository, AreaRepository areaRepository,
            RangoFechasResolver rangoFechasResolver, VencimientoService vencimientoService) {
        this.metricasTicketRepository = metricasTicketRepository;
        this.areaRepository = areaRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
    }

    private record Toma(Long ticketId, long minutos, long ordinalDerivado) {
    }

    public TiempoEsperaResumenDto calcular(LocalDate desde, LocalDate hasta) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);

        List<HistorialTransicionProjection> filas = metricasTicketRepository.historialParaTomasEn(rango.desde(), rango.hasta());
        Map<Long, List<HistorialTransicionProjection>> porTicket = filas.stream()
                .collect(Collectors.groupingBy(HistorialTransicionProjection::getTicketId, LinkedHashMap::new, Collectors.toList()));

        List<Toma> tomas = new ArrayList<>();
        for (Map.Entry<Long, List<HistorialTransicionProjection>> entrada : porTicket.entrySet()) {
            tomas.addAll(tomasDeTicket(entrada.getKey(), entrada.getValue(), rango));
        }

        if (tomas.isEmpty()) {
            return new TiempoEsperaResumenDto(desde, hasta, 0, null, null, List.of());
        }

        Map<String, Long> areaDestinoPorTomaKey = resolverAreasDestino(tomas);

        List<Long> minutosGlobal = tomas.stream().map(Toma::minutos).toList();

        Map<Long, List<Long>> minutosPorArea = new LinkedHashMap<>();
        for (Toma toma : tomas) {
            Long areaId = areaDestinoPorTomaKey.get(toma.ticketId() + ":" + toma.ordinalDerivado());
            if (areaId != null) {
                minutosPorArea.computeIfAbsent(areaId, k -> new ArrayList<>()).add(toma.minutos());
            }
        }

        Map<Long, String> nombresArea = areaRepository.findAllById(minutosPorArea.keySet()).stream()
                .collect(Collectors.toMap(Area::getId, Area::getNombre));

        List<TiempoEsperaPorAreaDto> porArea = minutosPorArea.entrySet().stream()
                .map(e -> new TiempoEsperaPorAreaDto(e.getKey(), nombresArea.get(e.getKey()), e.getValue().size(),
                        EstadisticasMinutosCalculator.promedio(e.getValue())))
                .sorted(Comparator.comparing(TiempoEsperaPorAreaDto::areaNombre))
                .toList();

        return new TiempoEsperaResumenDto(desde, hasta, tomas.size(),
                EstadisticasMinutosCalculator.promedio(minutosGlobal), EstadisticasMinutosCalculator.mediana(minutosGlobal), porArea);
    }

    private List<Toma> tomasDeTicket(Long ticketId, List<HistorialTransicionProjection> historial, RangoInstantes rango) {
        List<Toma> resultado = new ArrayList<>();
        long contadorDerivado = 0;
        for (int i = 0; i < historial.size(); i++) {
            HistorialTransicionProjection fila = historial.get(i);
            if (fila.getEstadoNuevo() == EstadoTicket.DERIVADO) {
                contadorDerivado++;
            }
            boolean esToma = i > 0 && fila.getEstadoAnterior() == EstadoTicket.DERIVADO && fila.getEstadoNuevo() == EstadoTicket.EN_PROGRESO;
            boolean enRango = !fila.getCreadoEn().isBefore(rango.desde()) && fila.getCreadoEn().isBefore(rango.hasta());
            if (esToma && enRango) {
                Instant inicio = historial.get(i - 1).getCreadoEn();
                long minutos = vencimientoService.minutosHabilesEntre(inicio, fila.getCreadoEn());
                resultado.add(new Toma(ticketId, minutos, contadorDerivado));
            }
        }
        return resultado;
    }

    private Map<String, Long> resolverAreasDestino(List<Toma> tomas) {
        Set<Long> ticketIds = tomas.stream().map(Toma::ticketId).collect(Collectors.toSet());
        List<DerivacionOrdinalProjection> ordinales = metricasTicketRepository.derivacionesOrdinales(ticketIds);
        return ordinales.stream()
                .collect(Collectors.toMap(o -> o.getTicketId() + ":" + o.getPosicion(), DerivacionOrdinalProjection::getAreaDestinoId));
    }
}

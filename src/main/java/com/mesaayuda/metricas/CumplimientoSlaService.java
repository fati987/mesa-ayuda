package com.mesaayuda.metricas;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.metricas.RangoFechasResolver.RangoInstantes;
import com.mesaayuda.metricas.dto.CumplimientoPorDimensionDto;
import com.mesaayuda.metricas.dto.CumplimientoSlaDto;
import com.mesaayuda.metricas.proyeccion.CumplimientoSlaProjection;
import com.mesaayuda.sla.VencimientoService;

/**
 * Universo: tickets con fechaVencimiento no nula (solo los que llegaron a
 * derivarse), filtrados por fechaVencimiento en el rango. "ahora" viene
 * del bean Clock existente (Sprint 4), no de now() de SQL, por
 * consistencia con TransicionService/EscalamientoService.
 */
@Service
@Transactional(readOnly = true)
public class CumplimientoSlaService {

    private final MetricasTicketRepository metricasTicketRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;
    private final Clock clock;

    public CumplimientoSlaService(MetricasTicketRepository metricasTicketRepository, RangoFechasResolver rangoFechasResolver,
            VencimientoService vencimientoService, Clock clock) {
        this.metricasTicketRepository = metricasTicketRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
        this.clock = clock;
    }

    public CumplimientoSlaDto calcular(LocalDate desde, LocalDate hasta) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);
        Instant ahora = clock.instant();

        List<CumplimientoSlaProjection> porCategoria = metricasTicketRepository.cumplimientoPorCategoria(rango.desde(), rango.hasta(), ahora);
        List<CumplimientoSlaProjection> porArea = metricasTicketRepository.cumplimientoPorArea(rango.desde(), rango.hasta(), ahora);
        List<CumplimientoSlaProjection> porPrioridad = metricasTicketRepository.cumplimientoPorPrioridad(rango.desde(), rango.hasta(), ahora);

        long total = porCategoria.stream().mapToLong(CumplimientoSlaProjection::getTotal).sum();
        long cumplidos = porCategoria.stream().mapToLong(CumplimientoSlaProjection::getCumplidos).sum();
        long incumplidos = porCategoria.stream().mapToLong(CumplimientoSlaProjection::getIncumplidos).sum();
        long enCurso = porCategoria.stream().mapToLong(CumplimientoSlaProjection::getEnCurso).sum();

        return new CumplimientoSlaDto(desde, hasta, total, cumplidos, incumplidos, enCurso,
                PorcentajeCalculator.calcular(cumplidos, cumplidos + incumplidos),
                aDto(porCategoria), aDto(porArea), aDto(porPrioridad));
    }

    private List<CumplimientoPorDimensionDto> aDto(List<CumplimientoSlaProjection> proyecciones) {
        return proyecciones.stream()
                .map(p -> new CumplimientoPorDimensionDto(p.getNombre(), p.getTotal(), p.getCumplidos(), p.getIncumplidos(), p.getEnCurso(),
                        PorcentajeCalculator.calcular(p.getCumplidos(), p.getCumplidos() + p.getIncumplidos())))
                .toList();
    }
}

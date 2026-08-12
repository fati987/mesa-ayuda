package com.mesaayuda.metricas;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.metricas.RangoFechasResolver.RangoInstantes;
import com.mesaayuda.metricas.dto.FcrPorAreaDto;
import com.mesaayuda.metricas.dto.FcrPorDiaDto;
import com.mesaayuda.metricas.dto.FcrResumenDto;
import com.mesaayuda.metricas.proyeccion.FcrPorDiaProjection;
import com.mesaayuda.metricas.proyeccion.ResumenAreaCreacionProjection;
import com.mesaayuda.sla.VencimientoService;

@Service
@Transactional(readOnly = true)
public class FcrService {

    private final MetricasTicketRepository metricasTicketRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;

    public FcrService(MetricasTicketRepository metricasTicketRepository, RangoFechasResolver rangoFechasResolver,
            VencimientoService vencimientoService) {
        this.metricasTicketRepository = metricasTicketRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
    }

    public FcrResumenDto calcular(LocalDate desde, LocalDate hasta) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);

        List<ResumenAreaCreacionProjection> porArea = metricasTicketRepository.resumenPorAreaCreacion(rango.desde(), rango.hasta());
        long totalCreados = porArea.stream().mapToLong(ResumenAreaCreacionProjection::getTotalCreados).sum();
        long resueltosEnLlamada = porArea.stream().mapToLong(ResumenAreaCreacionProjection::getResueltosEnLlamada).sum();

        List<FcrPorAreaDto> porAreaDto = porArea.stream()
                .map(p -> new FcrPorAreaDto(p.getAreaId(), p.getAreaNombre(), p.getTotalCreados(), p.getResueltosEnLlamada(),
                        PorcentajeCalculator.calcular(p.getResueltosEnLlamada(), p.getTotalCreados())))
                .sorted(Comparator.comparing(FcrPorAreaDto::areaNombre))
                .toList();

        List<FcrPorDiaProjection> porDia = metricasTicketRepository.fcrPorDia(rango.desde(), rango.hasta(), zona.getId());
        List<FcrPorDiaDto> porDiaDto = porDia.stream()
                .map(p -> new FcrPorDiaDto(p.getDia(), p.getTotalCreados(), p.getResueltosEnLlamada(),
                        PorcentajeCalculator.calcular(p.getResueltosEnLlamada(), p.getTotalCreados())))
                .toList();

        return new FcrResumenDto(desde, hasta, totalCreados, resueltosEnLlamada,
                PorcentajeCalculator.calcular(resueltosEnLlamada, totalCreados), porAreaDto, porDiaDto);
    }
}

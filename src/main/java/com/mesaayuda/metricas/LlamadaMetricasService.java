package com.mesaayuda.metricas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.metricas.RangoFechasResolver.RangoInstantes;
import com.mesaayuda.metricas.dto.LlamadaMetricasResumenDto;
import com.mesaayuda.metricas.dto.LlamadaMultiTicketDto;
import com.mesaayuda.metricas.proyeccion.LlamadaDuracionProjection;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.sla.VencimientoService;

@Service
@Transactional(readOnly = true)
public class LlamadaMetricasService {

    private final MetricasLlamadaRepository metricasLlamadaRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;

    public LlamadaMetricasService(MetricasLlamadaRepository metricasLlamadaRepository, RangoFechasResolver rangoFechasResolver,
            VencimientoService vencimientoService) {
        this.metricasLlamadaRepository = metricasLlamadaRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
    }

    public LlamadaMetricasResumenDto calcular(LocalDate desde, LocalDate hasta) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);

        LlamadaDuracionProjection duracion = metricasLlamadaRepository.duracionPromedio(rango.desde(), rango.hasta());
        long totalLlamadas = metricasLlamadaRepository.countByFechaHoraGreaterThanEqualAndFechaHoraLessThan(rango.desde(), rango.hasta());
        long llamadasConMultiplesTickets = metricasLlamadaRepository.llamadasConMultiplesTickets(rango.desde(), rango.hasta(), Pageable.ofSize(1))
                .getTotalElements();

        BigDecimal duracionPromedio = duracion.getPromedio() != null
                ? BigDecimal.valueOf(duracion.getPromedio()).setScale(2, RoundingMode.HALF_UP)
                : null;

        return new LlamadaMetricasResumenDto(desde, hasta, totalLlamadas, duracionPromedio,
                llamadasConMultiplesTickets, PorcentajeCalculator.calcular(llamadasConMultiplesTickets, totalLlamadas));
    }

    public PaginaResponse<LlamadaMultiTicketDto> llamadasConMultiplesTickets(LocalDate desde, LocalDate hasta, Pageable pageable) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);
        Page<Llamada> pagina = metricasLlamadaRepository.llamadasConMultiplesTickets(rango.desde(), rango.hasta(), pageable);
        return PaginaResponse.de(pagina, this::aDto);
    }

    private LlamadaMultiTicketDto aDto(Llamada llamada) {
        long cantidadTickets = metricasLlamadaRepository.contarTicketsDeLlamada(llamada.getId());
        return new LlamadaMultiTicketDto(llamada.getId(), llamada.getFechaHora(), llamada.getContacto().getNombreCompleto(), cantidadTickets);
    }
}

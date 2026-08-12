package com.mesaayuda.metricas;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.metricas.dto.FlujoAcumuladoDto;
import com.mesaayuda.metricas.dto.FlujoAcumuladoPuntoDto;
import com.mesaayuda.metricas.proyeccion.CfdPuntoProjection;
import com.mesaayuda.sla.VencimientoService;

/**
 * Único punto del sprint con SQL nativo justificado por complejidad, no
 * por rendimiento: reconstruir "estado vigente de cada ticket al final de
 * cada día del rango" no es razonable en JPQL. Tope de 366 días en el
 * rango, agregado por prudencia (no pedido explícitamente).
 */
@Service
@Transactional(readOnly = true)
public class FlujoAcumuladoService {

    private static final int RANGO_MAXIMO_DIAS = 366;

    private final MetricasTicketRepository metricasTicketRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;

    public FlujoAcumuladoService(MetricasTicketRepository metricasTicketRepository, RangoFechasResolver rangoFechasResolver,
            VencimientoService vencimientoService) {
        this.metricasTicketRepository = metricasTicketRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
    }

    public FlujoAcumuladoDto calcular(LocalDate desde, LocalDate hasta) {
        rangoFechasResolver.validarRangoMaximoDias(desde, hasta, RANGO_MAXIMO_DIAS);
        ZoneId zona = vencimientoService.zonaHorariaActiva();

        List<CfdPuntoProjection> puntos = metricasTicketRepository.flujoAcumulado(desde, hasta, zona.getId());
        List<FlujoAcumuladoPuntoDto> puntosDto = puntos.stream()
                .map(p -> new FlujoAcumuladoPuntoDto(p.getDia(), p.getEstado(), p.getCantidad()))
                .toList();

        return new FlujoAcumuladoDto(desde, hasta, puntosDto);
    }
}

package com.mesaayuda.metricas;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.metricas.RangoFechasResolver.RangoInstantes;
import com.mesaayuda.metricas.dto.TasaDerivacionPorAreaDto;
import com.mesaayuda.metricas.dto.TasaDerivacionResumenDto;
import com.mesaayuda.metricas.dto.TicketRebotadoDto;
import com.mesaayuda.metricas.proyeccion.ResumenAreaCreacionProjection;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.sla.VencimientoService;
import com.mesaayuda.ticket.Ticket;

@Service
@Transactional(readOnly = true)
public class TasaDerivacionService {

    private final MetricasTicketRepository metricasTicketRepository;
    private final RangoFechasResolver rangoFechasResolver;
    private final VencimientoService vencimientoService;

    public TasaDerivacionService(MetricasTicketRepository metricasTicketRepository, RangoFechasResolver rangoFechasResolver,
            VencimientoService vencimientoService) {
        this.metricasTicketRepository = metricasTicketRepository;
        this.rangoFechasResolver = rangoFechasResolver;
        this.vencimientoService = vencimientoService;
    }

    public TasaDerivacionResumenDto calcular(LocalDate desde, LocalDate hasta) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);

        List<ResumenAreaCreacionProjection> porArea = metricasTicketRepository.resumenPorAreaCreacion(rango.desde(), rango.hasta());
        long totalCreados = porArea.stream().mapToLong(ResumenAreaCreacionProjection::getTotalCreados).sum();
        long derivadosAlMenosUnaVez = porArea.stream().mapToLong(ResumenAreaCreacionProjection::getDerivadosAlMenosUnaVez).sum();

        long ticketsRebotados = metricasTicketRepository.contarTicketsRebotados(rango.desde(), rango.hasta());

        List<TasaDerivacionPorAreaDto> porAreaDto = porArea.stream()
                .map(p -> new TasaDerivacionPorAreaDto(p.getAreaId(), p.getAreaNombre(), p.getTotalCreados(), p.getDerivadosAlMenosUnaVez(),
                        PorcentajeCalculator.calcular(p.getDerivadosAlMenosUnaVez(), p.getTotalCreados())))
                .sorted(Comparator.comparing(TasaDerivacionPorAreaDto::areaNombre))
                .toList();

        return new TasaDerivacionResumenDto(desde, hasta, totalCreados, derivadosAlMenosUnaVez,
                PorcentajeCalculator.calcular(derivadosAlMenosUnaVez, totalCreados),
                ticketsRebotados, PorcentajeCalculator.calcular(ticketsRebotados, derivadosAlMenosUnaVez), porAreaDto);
    }

    public PaginaResponse<TicketRebotadoDto> rebotes(LocalDate desde, LocalDate hasta, Pageable pageable) {
        ZoneId zona = vencimientoService.zonaHorariaActiva();
        RangoInstantes rango = rangoFechasResolver.resolver(desde, hasta, zona);
        Page<Ticket> pagina = metricasTicketRepository.ticketsRebotados(rango.desde(), rango.hasta(), pageable);
        return PaginaResponse.de(pagina, this::aTicketRebotadoDto);
    }

    private TicketRebotadoDto aTicketRebotadoDto(Ticket ticket) {
        long cantidadDerivaciones = metricasTicketRepository.contarDerivaciones(ticket.getId());
        return new TicketRebotadoDto(ticket.getCodigo(), ticket.getAreaActual().getNombre(), cantidadDerivaciones);
    }
}

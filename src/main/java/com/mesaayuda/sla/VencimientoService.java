package com.mesaayuda.sla;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.ticket.enums.Prioridad;

/**
 * Orquesta la resolución del calendario laboral global y la política de
 * SLA activa por prioridad, y delega el cálculo puro a
 * CalendarioLaboralService. Sprint 4 asume un calendario laboral global
 * único; un modelo multi-calendario por área (ej. sedes en zonas horarias
 * distintas) queda para un sprint futuro si el negocio lo pide.
 */
@Service
@Transactional(readOnly = true)
public class VencimientoService {

    private final CalendarioLaboralRepository calendarioLaboralRepository;
    private final PoliticaSlaRepository politicaSlaRepository;
    private final FeriadoRepository feriadoRepository;
    private final CalendarioLaboralService calendarioLaboralService;

    public VencimientoService(CalendarioLaboralRepository calendarioLaboralRepository, PoliticaSlaRepository politicaSlaRepository,
            FeriadoRepository feriadoRepository, CalendarioLaboralService calendarioLaboralService) {
        this.calendarioLaboralRepository = calendarioLaboralRepository;
        this.politicaSlaRepository = politicaSlaRepository;
        this.feriadoRepository = feriadoRepository;
        this.calendarioLaboralService = calendarioLaboralService;
    }

    public Instant calcularVencimientoDerivacion(Instant instanteDerivacion, Prioridad prioridad) {
        CalendarioLaboral calendario = calendarioActivo();
        Set<LocalDate> feriados = feriados(calendario);
        int minutosSla = politicaActiva(prioridad).getMinutosResolucion();
        return calendarioLaboralService.calcularVencimiento(instanteDerivacion, minutosSla, calendario, feriados);
    }

    public long minutosHabilesEntre(Instant desde, Instant hasta) {
        CalendarioLaboral calendario = calendarioActivo();
        Set<LocalDate> feriados = feriados(calendario);
        return calendarioLaboralService.minutosHabilesEntre(desde, hasta, calendario, feriados);
    }

    /**
     * Expone la zona horaria del calendario activo para que los servicios
     * de métricas no dupliquen "buscar el calendario activo".
     */
    public ZoneId zonaHorariaActiva() {
        return calendarioActivo().zoneId();
    }

    private CalendarioLaboral calendarioActivo() {
        return calendarioLaboralRepository.findFirstByActivoTrueOrderByIdAsc()
                .orElseThrow(CalendarioLaboralNoConfiguradoException::new);
    }

    private PoliticaSla politicaActiva(Prioridad prioridad) {
        return politicaSlaRepository.findByPrioridadAndActivoTrue(prioridad)
                .orElseThrow(() -> new PoliticaSlaNoConfiguradaException(prioridad));
    }

    private Set<LocalDate> feriados(CalendarioLaboral calendario) {
        return feriadoRepository.findByCalendarioLaboral_Id(calendario.getId()).stream()
                .map(Feriado::getFecha)
                .collect(Collectors.toSet());
    }
}

package com.mesaayuda.metricas;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.metricas.dto.CumplimientoSlaDto;
import com.mesaayuda.metricas.dto.FcrResumenDto;
import com.mesaayuda.metricas.dto.FlujoAcumuladoDto;
import com.mesaayuda.metricas.dto.LlamadaMetricasResumenDto;
import com.mesaayuda.metricas.dto.LlamadaMultiTicketDto;
import com.mesaayuda.metricas.dto.TasaDerivacionResumenDto;
import com.mesaayuda.metricas.dto.TicketRebotadoDto;
import com.mesaayuda.metricas.dto.TiempoCicloResumenDto;
import com.mesaayuda.metricas.dto.TiempoEsperaResumenDto;
import com.mesaayuda.shared.paginacion.PaginaResponse;

@RestController
@RequestMapping("/api/metricas")
@PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
public class MetricasController {

    private final FcrService fcrService;
    private final TasaDerivacionService tasaDerivacionService;
    private final TiempoCicloService tiempoCicloService;
    private final TiempoEsperaService tiempoEsperaService;
    private final CumplimientoSlaService cumplimientoSlaService;
    private final FlujoAcumuladoService flujoAcumuladoService;
    private final LlamadaMetricasService llamadaMetricasService;

    public MetricasController(FcrService fcrService, TasaDerivacionService tasaDerivacionService, TiempoCicloService tiempoCicloService,
            TiempoEsperaService tiempoEsperaService, CumplimientoSlaService cumplimientoSlaService,
            FlujoAcumuladoService flujoAcumuladoService, LlamadaMetricasService llamadaMetricasService) {
        this.fcrService = fcrService;
        this.tasaDerivacionService = tasaDerivacionService;
        this.tiempoCicloService = tiempoCicloService;
        this.tiempoEsperaService = tiempoEsperaService;
        this.cumplimientoSlaService = cumplimientoSlaService;
        this.flujoAcumuladoService = flujoAcumuladoService;
        this.llamadaMetricasService = llamadaMetricasService;
    }

    @GetMapping("/fcr")
    public FcrResumenDto fcr(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return fcrService.calcular(desde, hasta);
    }

    @GetMapping("/tasa-derivacion")
    public TasaDerivacionResumenDto tasaDerivacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return tasaDerivacionService.calcular(desde, hasta);
    }

    @GetMapping("/tasa-derivacion/rebotes")
    public PaginaResponse<TicketRebotadoDto> rebotes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return tasaDerivacionService.rebotes(desde, hasta, pageable);
    }

    @GetMapping("/tiempo-ciclo")
    public TiempoCicloResumenDto tiempoCiclo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return tiempoCicloService.calcular(desde, hasta);
    }

    @GetMapping("/tiempo-espera")
    public TiempoEsperaResumenDto tiempoEspera(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return tiempoEsperaService.calcular(desde, hasta);
    }

    @GetMapping("/cumplimiento-sla")
    public CumplimientoSlaDto cumplimientoSla(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return cumplimientoSlaService.calcular(desde, hasta);
    }

    @GetMapping("/flujo-acumulado")
    public FlujoAcumuladoDto flujoAcumulado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return flujoAcumuladoService.calcular(desde, hasta);
    }

    @GetMapping("/llamadas")
    public LlamadaMetricasResumenDto llamadas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return llamadaMetricasService.calcular(desde, hasta);
    }

    @GetMapping("/llamadas/multiples-tickets")
    public PaginaResponse<LlamadaMultiTicketDto> llamadasConMultiplesTickets(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return llamadaMetricasService.llamadasConMultiplesTickets(desde, hasta, pageable);
    }
}

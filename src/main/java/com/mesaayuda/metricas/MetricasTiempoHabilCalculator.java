package com.mesaayuda.metricas;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mesaayuda.sla.VencimientoService;

/**
 * Convierte un intervalo [inicio, fin] menos una lista de pausas en
 * minutos hábiles, reutilizando VencimientoService.minutosHabilesEntre —
 * el mismo mecanismo que ya usa TransicionService.aplicarReanudacion, para
 * que lead time/cycle time/tiempo de espera midan exactamente lo mismo
 * que el reloj de SLA real, no una reimplementación paralela.
 */
@Component
public class MetricasTiempoHabilCalculator {

    private final VencimientoService vencimientoService;

    public MetricasTiempoHabilCalculator(VencimientoService vencimientoService) {
        this.vencimientoService = vencimientoService;
    }

    public record Pausa(Instant inicio, Instant fin) {
    }

    public long minutosHabilesDescontandoPausas(Instant inicio, Instant fin, List<Pausa> pausas) {
        long total = vencimientoService.minutosHabilesEntre(inicio, fin);
        long pausado = pausas.stream()
                .mapToLong(p -> vencimientoService.minutosHabilesEntre(p.inicio(), p.fin()))
                .sum();
        return Math.max(total - pausado, 0);
    }
}

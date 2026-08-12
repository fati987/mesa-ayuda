package com.mesaayuda.sla;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.TicketRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EscalamientoJob {

    private final TicketRepository ticketRepository;
    private final EscalamientoService escalamientoService;

    public EscalamientoJob(TicketRepository ticketRepository, EscalamientoService escalamientoService) {
        this.ticketRepository = ticketRepository;
        this.escalamientoService = escalamientoService;
    }

    @Scheduled(cron = "${sla.escalamiento.cron}")
    public void ejecutar() {
        List<Long> candidatos = ticketRepository.buscarCandidatosEscalamiento().stream().map(Ticket::getId).toList();
        for (Long id : candidatos) {
            try {
                escalamientoService.escalarSiCorresponde(id);
            } catch (Exception ex) {
                // Un ticket con error no debe tumbar el resto del job.
                log.error("No se pudo procesar el escalamiento del ticket id={}", id, ex);
            }
        }
    }
}

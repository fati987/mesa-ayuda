package com.mesaayuda.ticket;

import java.time.Year;

import org.springframework.stereotype.Component;

@Component
public class TicketCodigoGenerator {

    private final TicketRepository ticketRepository;

    public TicketCodigoGenerator(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public String generar() {
        Long numero = ticketRepository.siguienteNumeroCodigo();
        return "SOP-%d-%04d".formatted(Year.now().getValue(), numero);
    }
}

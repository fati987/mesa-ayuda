package com.mesaayuda.ticket;

import org.springframework.stereotype.Service;

import com.mesaayuda.area.Area;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.usuario.Usuario;

/**
 * Guarda de la regla invariable #7: un agente no puede superar el
 * limiteWipAgente de su área en estado EN_PROGRESO.
 */
@Service
public class AsignacionService {

    private final TicketRepository ticketRepository;

    public AsignacionService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public void verificarLimiteWip(Usuario agente, Area area) {
        long enProgreso = ticketRepository.countByUsuarioAsignado_IdAndEstado(agente.getId(), EstadoTicket.EN_PROGRESO);
        if (enProgreso >= area.getLimiteWipAgente()) {
            throw new LimiteWipSuperadoException(enProgreso, area.getLimiteWipAgente());
        }
    }
}

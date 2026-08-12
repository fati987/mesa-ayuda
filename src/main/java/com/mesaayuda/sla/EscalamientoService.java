package com.mesaayuda.sla;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.notificacion.NotificacionService;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.TicketRepository;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.UsuarioRepository;
import com.mesaayuda.usuario.enums.Rol;

/**
 * Un ticket por corrida del job, cada uno en su propia transacción (por
 * eso vive en un bean distinto de EscalamientoJob: la auto-invocación
 * dentro de la misma clase no respeta @Transactional en Spring).
 */
@Service
public class EscalamientoService {

    private final TicketRepository ticketRepository;
    private final VencimientoService vencimientoService;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final SlaEscalamientoProperties propiedades;
    private final Clock clock;

    public EscalamientoService(TicketRepository ticketRepository, VencimientoService vencimientoService,
            UsuarioRepository usuarioRepository, NotificacionService notificacionService,
            SlaEscalamientoProperties propiedades, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.vencimientoService = vencimientoService;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
        this.propiedades = propiedades;
        this.clock = clock;
    }

    @Transactional
    public void escalarSiCorresponde(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalStateException("Ticket id=%d no encontrado durante el escalamiento".formatted(ticketId)));

        // Recarga defensiva: el estado pudo cambiar entre el query de
        // candidatos y este punto (otra transacción concurrente).
        if (ticket.isEscalado() || ticket.getFechaVencimiento() == null
                || (ticket.getEstado() != EstadoTicket.DERIVADO && ticket.getEstado() != EstadoTicket.EN_PROGRESO)) {
            return;
        }

        Instant ahora = clock.instant();
        long minutosRestantes = ticket.getFechaVencimiento().isBefore(ahora)
                ? 0
                : vencimientoService.minutosHabilesEntre(ahora, ticket.getFechaVencimiento());
        if (minutosRestantes > propiedades.umbralMinutos()) {
            return;
        }

        subirPrioridad(ticket);
        ticket.setEscalado(true);

        List<Usuario> supervisores = usuarioRepository.findByRolAndArea_IdAndActivoTrue(Rol.SUPERVISOR, ticket.getAreaActual().getId());
        notificacionService.notificarEscalamiento(ticket, supervisores);
    }

    private void subirPrioridad(Ticket ticket) {
        Prioridad[] valores = Prioridad.values();
        int siguiente = Math.min(ticket.getPrioridad().ordinal() + 1, valores.length - 1);
        ticket.setPrioridad(valores[siguiente]);
    }
}

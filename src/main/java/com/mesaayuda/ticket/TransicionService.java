package com.mesaayuda.ticket;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.Area;
import com.mesaayuda.derivacion.DerivacionService;
import com.mesaayuda.sla.VencimientoService;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.UsuarioRepository;

/**
 * Dueño de la orquestación de cada transición: valida el par (origen,
 * destino) contra el grafo de EstadoTicket, aplica la guarda y el efecto
 * lateral específicos, cambia el estado del ticket e inserta la fila de
 * HistorialEstado — todo en una única transacción (cambiar estado y
 * escribir historial es atómico o no es).
 *
 * Este mismo método es el punto de extensión para un futuro job de cierre
 * automático (RESUELTO->CERRADO "por job tras N días"): ese job solo
 * tendrá que invocar transicionar(...) igual que el endpoint manual, sin
 * cambios acá.
 *
 * CERRADO->EN_PROGRESO (reapertura) no tiene todavía guarda de "ventana de
 * tiempo": no hay valor de N días definido para eso en el CLAUDE.md.
 * Comportamiento temporal, no definitivo.
 *
 * Desde Sprint 4, aplicarDerivacion calcula fechaVencimiento (solo la
 * primera vez que el ticket sale de NUEVO) y aplicarReanudacion corre ese
 * vencimiento hacia adelante por los minutos hábiles que estuvo pausado —
 * ambos vía VencimientoService, que encapsula el cálculo de horario hábil.
 */
@Service
public class TransicionService {

    private final HistorialEstadoRepository historialEstadoRepository;
    private final AsignacionService asignacionService;
    private final DerivacionService derivacionService;
    private final UsuarioRepository usuarioRepository;
    private final VencimientoService vencimientoService;
    private final Clock clock;

    public TransicionService(HistorialEstadoRepository historialEstadoRepository, AsignacionService asignacionService,
            DerivacionService derivacionService, UsuarioRepository usuarioRepository, VencimientoService vencimientoService,
            Clock clock) {
        this.historialEstadoRepository = historialEstadoRepository;
        this.asignacionService = asignacionService;
        this.derivacionService = derivacionService;
        this.usuarioRepository = usuarioRepository;
        this.vencimientoService = vencimientoService;
        this.clock = clock;
    }

    @Transactional
    public Ticket transicionar(Ticket ticket, EstadoTicket destino, ContextoTransicion contexto) {
        EstadoTicket origen = ticket.getEstado();
        if (!origen.puedeTransicionarA(destino)) {
            throw new TransicionInvalidaException(origen, destino);
        }

        Usuario ejecutor = usuarioRepository.getReferenceById(contexto.ejecutor().getId());

        switch (origen) {
            case NUEVO -> {
                if (destino == EstadoTicket.RESUELTO) {
                    aplicarResolucionEnLlamada(ticket);
                } else {
                    aplicarDerivacion(ticket, contexto, ejecutor);
                }
            }
            case DERIVADO -> {
                if (destino == EstadoTicket.EN_PROGRESO) {
                    aplicarToma(ticket, contexto, ejecutor);
                } else {
                    aplicarDerivacion(ticket, contexto, ejecutor);
                }
            }
            case EN_PROGRESO -> {
                if (destino == EstadoTicket.RESUELTO) {
                    aplicarResolucion(ticket, contexto);
                } else if (destino == EstadoTicket.DERIVADO) {
                    aplicarDerivacion(ticket, contexto, ejecutor);
                    ticket.setUsuarioAsignado(null);
                }
                // destino == ESPERANDO_CLIENTE: sin guarda ni efecto lateral
            }
            case ESPERANDO_CLIENTE -> aplicarReanudacion(ticket);
            case CERRADO -> ticket.setUsuarioAsignado(ejecutor);
            case RESUELTO -> {
                // RESUELTO->CERRADO manual: sin guarda de datos adicional
            }
        }

        ticket.setEstado(destino);
        historialEstadoRepository.save(construirHistorial(ticket, origen, destino, ejecutor, contexto));
        return ticket;
    }

    private void aplicarResolucionEnLlamada(Ticket ticket) {
        if (!ticket.isResueltoEnLlamada()) {
            throw new TicketNoResueltoEnLlamadaException();
        }
        if (!ticket.getAreaActual().isRecibeLlamadas()) {
            throw new AreaNoRecibeLlamadasException(ticket.getAreaActual().getNombre());
        }
    }

    private void aplicarDerivacion(Ticket ticket, ContextoTransicion contexto, Usuario ejecutor) {
        Area areaDestino = contexto.areaDestino();
        if (areaDestino == null || areaDestino.getId().equals(ticket.getAreaActual().getId())) {
            throw DerivacionInvalidaException.areaDestinoIgualALaActual();
        }
        String motivo = contexto.motivoDerivacion();
        if (motivo == null || motivo.isBlank()) {
            throw DerivacionInvalidaException.motivoVacio();
        }
        derivacionService.registrar(ticket, ticket.getAreaActual(), areaDestino, ejecutor, motivo);
        ticket.setAreaActual(areaDestino);

        // Regla invariable #4: el reloj arranca en la derivación, no en la
        // creación. Solo se calcula la primera vez — los rebotes y la
        // segunda derivación no lo recalculan (la política depende de la
        // prioridad, no del área; resetear por rebotar permitiría "comprar
        // tiempo" derivando en bucle).
        if (ticket.getFechaVencimiento() == null) {
            Instant instanteDerivacion = clock.instant();
            ticket.setFechaVencimiento(vencimientoService.calcularVencimientoDerivacion(instanteDerivacion, ticket.getPrioridad()));
        }
    }

    private void aplicarToma(Ticket ticket, ContextoTransicion contexto, Usuario ejecutor) {
        if (!contexto.ejecutor().getAreaId().equals(ticket.getAreaActual().getId())) {
            throw new AgenteFueraDeAreaException();
        }
        asignacionService.verificarLimiteWip(ejecutor, ticket.getAreaActual());
        ticket.setUsuarioAsignado(ejecutor);
    }

    private void aplicarResolucion(Ticket ticket, ContextoTransicion contexto) {
        String solucion = contexto.solucion();
        if (solucion == null || solucion.isBlank()) {
            throw new SolucionRequeridaException();
        }
        ticket.setSolucion(solucion);
    }

    private void aplicarReanudacion(Ticket ticket) {
        Instant pausadoDesde = ticket.getActualizadoEn();
        Instant ahora = clock.instant();
        long minutosHabiles = Math.max(vencimientoService.minutosHabilesEntre(pausadoDesde, ahora), 0);
        ticket.setMinutosPausado(ticket.getMinutosPausado() + (int) minutosHabiles);
        // El guard != null es necesario porque los tickets semilla que ya
        // están en flujo no tienen vencimiento retroactivo (Sprint 4 no
        // hace backfill).
        if (ticket.getFechaVencimiento() != null) {
            ticket.setFechaVencimiento(ticket.getFechaVencimiento().plus(minutosHabiles, ChronoUnit.MINUTES));
        }
    }

    private HistorialEstado construirHistorial(Ticket ticket, EstadoTicket origen, EstadoTicket destino, Usuario ejecutor,
            ContextoTransicion contexto) {
        HistorialEstado historial = new HistorialEstado();
        historial.setTicket(ticket);
        historial.setEstadoAnterior(origen);
        historial.setEstadoNuevo(destino);
        historial.setUsuario(ejecutor);
        historial.setComentario(contexto.comentario());
        return historial;
    }
}

package com.mesaayuda.tablero;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mesaayuda.ticket.TableroActualizadoEvento;

/**
 * AFTER_COMMIT es deliberado: si se publicara el mensaje STOMP dentro de la
 * transacción de TransicionService y esta hiciera rollback (ej.
 * LimiteWipSuperadoException, TicketNoResueltoEnLlamadaException), se
 * notificaría un movimiento de tablero que en realidad nunca pasó.
 */
@Component
public class TableroEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public TableroEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alActualizarTablero(TableroActualizadoEvento evento) {
        messagingTemplate.convertAndSend("/topic/tablero/" + evento.areaId(), evento);
    }
}

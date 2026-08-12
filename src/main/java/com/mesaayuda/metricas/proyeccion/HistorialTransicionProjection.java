package com.mesaayuda.metricas.proyeccion;

import java.time.Instant;

import com.mesaayuda.ticket.enums.EstadoTicket;

public interface HistorialTransicionProjection {

    Long getTicketId();

    String getCodigo();

    EstadoTicket getEstadoAnterior();

    EstadoTicket getEstadoNuevo();

    Instant getCreadoEn();
}

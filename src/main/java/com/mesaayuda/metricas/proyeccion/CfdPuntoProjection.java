package com.mesaayuda.metricas.proyeccion;

import java.time.LocalDate;

import com.mesaayuda.ticket.enums.EstadoTicket;

public interface CfdPuntoProjection {

    LocalDate getDia();

    EstadoTicket getEstado();

    long getCantidad();
}

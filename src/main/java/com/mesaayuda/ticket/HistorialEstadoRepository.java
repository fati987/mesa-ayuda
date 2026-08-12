package com.mesaayuda.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByTicketIdOrderByCreadoEnAsc(Long ticketId);
}

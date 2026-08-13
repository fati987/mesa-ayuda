package com.mesaayuda.derivacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {

    List<Derivacion> findByTicket_IdOrderByCreadoEnAsc(Long ticketId);
}

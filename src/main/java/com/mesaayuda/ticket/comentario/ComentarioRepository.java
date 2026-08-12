package com.mesaayuda.ticket.comentario;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mesaayuda.ticket.enums.Visibilidad;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    Page<Comentario> findByTicketIdAndVisibilidadInOrderByCreadoEnAsc(Long ticketId, List<Visibilidad> visibilidades, Pageable pageable);
}

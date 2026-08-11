package com.mesaayuda.ticket;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Prioridad;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoAndEliminadoEnIsNull(String codigo);

    @Query(value = """
            select t from Ticket t
            join fetch t.areaActual
            join fetch t.categoria
            join fetch t.contacto
            where (:estado is null or t.estado = :estado)
            and (:prioridad is null or t.prioridad = :prioridad)
            and (:areaId is null or t.areaActual.id = :areaId)
            and t.eliminadoEn is null
            """,
            countQuery = """
            select count(t) from Ticket t
            where (:estado is null or t.estado = :estado)
            and (:prioridad is null or t.prioridad = :prioridad)
            and (:areaId is null or t.areaActual.id = :areaId)
            and t.eliminadoEn is null
            """)
    Page<Ticket> buscar(
            @Param("estado") EstadoTicket estado,
            @Param("prioridad") Prioridad prioridad,
            @Param("areaId") Long areaId,
            Pageable pageable);
}

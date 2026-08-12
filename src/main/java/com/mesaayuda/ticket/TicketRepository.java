package com.mesaayuda.ticket;

import java.util.List;
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

    @Query(value = "select nextval('ticket_codigo_seq')", nativeQuery = true)
    Long siguienteNumeroCodigo();

    long countByUsuarioAsignado_IdAndEstado(Long usuarioAsignadoId, EstadoTicket estado);

    // Candidatos a escalamiento: solo DERIVADO/EN_PROGRESO tienen el reloj
    // de SLA corriendo de verdad (ESPERANDO_CLIENTE está pausado, regla
    // invariable #6, su fechaVencimiento puede estar vencida en reloj de
    // pared sin que el ticket esté realmente vencido). Usa el índice
    // compuesto (estado, fecha_vencimiento) de V7.
    @Query("""
            select t from Ticket t
            join fetch t.areaActual
            where t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.DERIVADO, com.mesaayuda.ticket.enums.EstadoTicket.EN_PROGRESO)
            and t.fechaVencimiento is not null
            and t.escalado = false
            and t.eliminadoEn is null
            order by t.fechaVencimiento asc
            """)
    List<Ticket> buscarCandidatosEscalamiento();

    @Query(value = """
            select t from Ticket t
            join fetch t.areaActual
            join fetch t.categoria
            join fetch t.contacto
            where t.areaActual.id = :areaId
            and t.estado = :estado
            and t.resueltoEnLlamada = false
            and t.eliminadoEn is null
            """,
            countQuery = """
            select count(t) from Ticket t
            where t.areaActual.id = :areaId
            and t.estado = :estado
            and t.resueltoEnLlamada = false
            and t.eliminadoEn is null
            """)
    Page<Ticket> buscarParaTablero(@Param("areaId") Long areaId, @Param("estado") EstadoTicket estado, Pageable pageable);

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

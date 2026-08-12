package com.mesaayuda.metricas;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.mesaayuda.metricas.proyeccion.CfdPuntoProjection;
import com.mesaayuda.metricas.proyeccion.CumplimientoSlaProjection;
import com.mesaayuda.metricas.proyeccion.DerivacionOrdinalProjection;
import com.mesaayuda.metricas.proyeccion.FcrPorDiaProjection;
import com.mesaayuda.metricas.proyeccion.HistorialTransicionProjection;
import com.mesaayuda.metricas.proyeccion.ResumenAreaCreacionProjection;
import com.mesaayuda.ticket.Ticket;

/**
 * Repositorio de solo lectura para reporting: consultas de agregación más
 * pesadas que las operacionales de TicketRepository (subconsultas
 * correlacionadas, LATERAL, generate_series). Marcador Repository&lt;Ticket,Long&gt;
 * en vez de JpaRepository porque no necesita save/findById, solo las
 * consultas custom.
 */
public interface MetricasTicketRepository extends Repository<Ticket, Long> {

    // FCR (por área) + tasa de derivación por área: misma base, área de
    // creación = área origen de la primera Derivacion del ticket, o su
    // área actual si nunca se derivó.
    @Query(value = """
            select coalesce(fd.area_origen_id, t.area_actual_id) as areaId,
                   a.nombre as areaNombre,
                   count(*) as totalCreados,
                   count(*) filter (where t.resuelto_en_llamada and t.estado in ('RESUELTO','CERRADO')) as resueltosEnLlamada,
                   count(*) filter (where fd.area_origen_id is not null) as derivadosAlMenosUnaVez
            from ticket t
            left join lateral (
                select d.area_origen_id
                from derivacion d
                where d.ticket_id = t.id
                order by d.creado_en asc
                limit 1
            ) fd on true
            join area a on a.id = coalesce(fd.area_origen_id, t.area_actual_id)
            where t.llamada_id is not null and t.eliminado_en is null
              and t.creado_en >= :desde and t.creado_en < :hasta
            group by coalesce(fd.area_origen_id, t.area_actual_id), a.nombre
            """, nativeQuery = true)
    List<ResumenAreaCreacionProjection> resumenPorAreaCreacion(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    // FCR por día, en la zona horaria del calendario laboral activo.
    @Query(value = """
            select (t.creado_en at time zone :zona)::date as dia,
                   count(*) as totalCreados,
                   count(*) filter (where t.resuelto_en_llamada and t.estado in ('RESUELTO','CERRADO')) as resueltosEnLlamada
            from ticket t
            where t.llamada_id is not null and t.eliminado_en is null
              and t.creado_en >= :desde and t.creado_en < :hasta
            group by 1 order by 1
            """, nativeQuery = true)
    List<FcrPorDiaProjection> fcrPorDia(@Param("desde") Instant desde, @Param("hasta") Instant hasta, @Param("zona") String zona);

    // Rebote: tickets con más de una fila en Derivacion, creados en el rango.
    @Query(value = """
            select t from Ticket t join fetch t.areaActual
            where t.id in (select d.ticket.id from Derivacion d group by d.ticket.id having count(d) > 1)
            and t.creadoEn >= :desde and t.creadoEn < :hasta and t.eliminadoEn is null
            """,
            countQuery = """
            select count(t) from Ticket t
            where t.id in (select d.ticket.id from Derivacion d group by d.ticket.id having count(d) > 1)
            and t.creadoEn >= :desde and t.creadoEn < :hasta and t.eliminadoEn is null
            """)
    Page<Ticket> ticketsRebotados(@Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);

    @Query("""
            select count(t) from Ticket t
            where t.id in (select d.ticket.id from Derivacion d group by d.ticket.id having count(d) > 1)
            and t.creadoEn >= :desde and t.creadoEn < :hasta and t.eliminadoEn is null
            """)
    long contarTicketsRebotados(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    @Query("select count(d) from Derivacion d where d.ticket.id = :ticketId")
    long contarDerivaciones(@Param("ticketId") Long ticketId);

    // Insumo crudo (todo el historial por ticket) para lead time/cycle
    // time: tickets cuya PRIMERA resolución cae en el rango.
    @Query("""
            select h.ticket.id as ticketId, h.ticket.codigo as codigo,
                   h.estadoAnterior as estadoAnterior, h.estadoNuevo as estadoNuevo, h.creadoEn as creadoEn
            from HistorialEstado h
            where h.ticket.id in (
                select h2.ticket.id from HistorialEstado h2
                where h2.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO
                group by h2.ticket.id
                having min(h2.creadoEn) >= :desde and min(h2.creadoEn) < :hasta
            )
            order by h.ticket.id, h.creadoEn
            """)
    List<HistorialTransicionProjection> historialParaTicketsResueltosEn(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    // Insumo crudo para tiempo de espera previo: tickets con al menos una
    // toma (DERIVADO->EN_PROGRESO) cuya fecha cae en el rango.
    @Query("""
            select h.ticket.id as ticketId, h.ticket.codigo as codigo,
                   h.estadoAnterior as estadoAnterior, h.estadoNuevo as estadoNuevo, h.creadoEn as creadoEn
            from HistorialEstado h
            where h.ticket.id in (
                select h2.ticket.id from HistorialEstado h2
                where h2.estadoAnterior = com.mesaayuda.ticket.enums.EstadoTicket.DERIVADO
                  and h2.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.EN_PROGRESO
                  and h2.creadoEn >= :desde and h2.creadoEn < :hasta
            )
            order by h.ticket.id, h.creadoEn
            """)
    List<HistorialTransicionProjection> historialParaTomasEn(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    // Área destino por posición ordinal (la N-ésima entrada a DERIVADO de
    // un ticket = la N-ésima fila de Derivacion de ese ticket).
    @Query(value = """
            select ticket_id as ticketId, area_destino_id as areaDestinoId,
                   row_number() over (partition by ticket_id order by creado_en) as posicion
            from derivacion where ticket_id in (:ticketIds)
            """, nativeQuery = true)
    List<DerivacionOrdinalProjection> derivacionesOrdinales(@Param("ticketIds") Collection<Long> ticketIds);

    @Query("""
            select t.categoria.nombre as nombre, count(t) as total,
              sum(case when t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO)
                        and (select min(h.creadoEn) from HistorialEstado h where h.ticket = t and h.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO) <= t.fechaVencimiento
                       then 1L else 0L end) as cumplidos,
              sum(case when (t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO)
                              and (select min(h.creadoEn) from HistorialEstado h where h.ticket = t and h.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO) > t.fechaVencimiento)
                         or (t.estado not in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO) and t.fechaVencimiento < :ahora)
                       then 1L else 0L end) as incumplidos,
              sum(case when t.estado not in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO) and t.fechaVencimiento >= :ahora
                       then 1L else 0L end) as enCurso
            from Ticket t
            where t.fechaVencimiento is not null and t.eliminadoEn is null
              and t.fechaVencimiento >= :desde and t.fechaVencimiento < :hasta
            group by t.categoria.nombre
            """)
    List<CumplimientoSlaProjection> cumplimientoPorCategoria(@Param("desde") Instant desde, @Param("hasta") Instant hasta, @Param("ahora") Instant ahora);

    @Query("""
            select t.areaActual.nombre as nombre, count(t) as total,
              sum(case when t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO)
                        and (select min(h.creadoEn) from HistorialEstado h where h.ticket = t and h.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO) <= t.fechaVencimiento
                       then 1L else 0L end) as cumplidos,
              sum(case when (t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO)
                              and (select min(h.creadoEn) from HistorialEstado h where h.ticket = t and h.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO) > t.fechaVencimiento)
                         or (t.estado not in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO) and t.fechaVencimiento < :ahora)
                       then 1L else 0L end) as incumplidos,
              sum(case when t.estado not in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO) and t.fechaVencimiento >= :ahora
                       then 1L else 0L end) as enCurso
            from Ticket t
            where t.fechaVencimiento is not null and t.eliminadoEn is null
              and t.fechaVencimiento >= :desde and t.fechaVencimiento < :hasta
            group by t.areaActual.nombre
            """)
    List<CumplimientoSlaProjection> cumplimientoPorArea(@Param("desde") Instant desde, @Param("hasta") Instant hasta, @Param("ahora") Instant ahora);

    @Query("""
            select cast(t.prioridad as string) as nombre, count(t) as total,
              sum(case when t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO)
                        and (select min(h.creadoEn) from HistorialEstado h where h.ticket = t and h.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO) <= t.fechaVencimiento
                       then 1L else 0L end) as cumplidos,
              sum(case when (t.estado in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO)
                              and (select min(h.creadoEn) from HistorialEstado h where h.ticket = t and h.estadoNuevo = com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO) > t.fechaVencimiento)
                         or (t.estado not in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO) and t.fechaVencimiento < :ahora)
                       then 1L else 0L end) as incumplidos,
              sum(case when t.estado not in (com.mesaayuda.ticket.enums.EstadoTicket.RESUELTO, com.mesaayuda.ticket.enums.EstadoTicket.CERRADO) and t.fechaVencimiento >= :ahora
                       then 1L else 0L end) as enCurso
            from Ticket t
            where t.fechaVencimiento is not null and t.eliminadoEn is null
              and t.fechaVencimiento >= :desde and t.fechaVencimiento < :hasta
            group by t.prioridad
            """)
    List<CumplimientoSlaProjection> cumplimientoPorPrioridad(@Param("desde") Instant desde, @Param("hasta") Instant hasta, @Param("ahora") Instant ahora);

    // Diagrama de flujo acumulado: único caso con SQL nativo, reconstruye
    // el estado vigente de cada ticket al final de cada día del rango.
    @Query(value = """
            with dias as (
              select generate_series((:desde)::date, (:hasta)::date, interval '1 day')::date as dia
            )
            select d.dia as dia, h.estado as estado, count(*) as cantidad
            from dias d
            join ticket t on t.creado_en <= ((d.dia + 1)::timestamp at time zone :zona) and t.eliminado_en is null
            join lateral (
              select he.estado_nuevo as estado
              from historial_estado he
              where he.ticket_id = t.id
                and he.creado_en <= ((d.dia + 1)::timestamp at time zone :zona)
              order by he.creado_en desc
              limit 1
            ) h on true
            group by d.dia, h.estado
            order by d.dia, h.estado
            """, nativeQuery = true)
    List<CfdPuntoProjection> flujoAcumulado(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("zona") String zona);
}

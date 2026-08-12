package com.mesaayuda.metricas;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.metricas.proyeccion.LlamadaDuracionProjection;

public interface MetricasLlamadaRepository extends Repository<Llamada, Long> {

    @Query("select avg(l.duracionSegundos) as promedio, count(l) as total from Llamada l "
            + "where l.fechaHora >= :desde and l.fechaHora < :hasta and l.duracionSegundos is not null")
    LlamadaDuracionProjection duracionPromedio(@Param("desde") Instant desde, @Param("hasta") Instant hasta);

    @Query(value = "select l from Llamada l where l.fechaHora >= :desde and l.fechaHora < :hasta "
            + "and l.id in (select t.llamada.id from Ticket t where t.llamada is not null group by t.llamada.id having count(t) > 1)",
            countQuery = "select count(l) from Llamada l where l.fechaHora >= :desde and l.fechaHora < :hasta "
                    + "and l.id in (select t.llamada.id from Ticket t where t.llamada is not null group by t.llamada.id having count(t) > 1)")
    Page<Llamada> llamadasConMultiplesTickets(@Param("desde") Instant desde, @Param("hasta") Instant hasta, Pageable pageable);

    @Query("select count(t) from Ticket t where t.llamada.id = :llamadaId")
    long contarTicketsDeLlamada(@Param("llamadaId") Long llamadaId);

    long countByFechaHoraGreaterThanEqualAndFechaHoraLessThan(Instant desde, Instant hasta);
}

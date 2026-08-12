package com.mesaayuda.sla;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mesaayuda.ticket.enums.Prioridad;

public interface PoliticaSlaRepository extends JpaRepository<PoliticaSla, Long> {

    Optional<PoliticaSla> findByPrioridadAndActivoTrue(Prioridad prioridad);
}

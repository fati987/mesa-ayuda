package com.mesaayuda.sla;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarioLaboralRepository extends JpaRepository<CalendarioLaboral, Long> {

    Optional<CalendarioLaboral> findFirstByActivoTrueOrderByIdAsc();
}

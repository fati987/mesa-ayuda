package com.mesaayuda.sla;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {

    List<Feriado> findByCalendarioLaboral_Id(Long calendarioLaboralId);
}

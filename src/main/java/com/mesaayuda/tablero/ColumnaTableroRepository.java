package com.mesaayuda.tablero;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ColumnaTableroRepository extends JpaRepository<ColumnaTablero, Long> {

    List<ColumnaTablero> findByAreaIdAndActivoTrueOrderByOrdenAsc(Long areaId);
}

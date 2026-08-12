package com.mesaayuda.area;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, Long> {

    boolean existsByNombre(String nombre);
}

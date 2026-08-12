package com.mesaayuda.metricas.proyeccion;

import java.time.LocalDate;

public interface FcrPorDiaProjection {

    LocalDate getDia();

    long getTotalCreados();

    long getResueltosEnLlamada();
}

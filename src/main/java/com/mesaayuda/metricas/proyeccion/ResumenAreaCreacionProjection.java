package com.mesaayuda.metricas.proyeccion;

public interface ResumenAreaCreacionProjection {

    Long getAreaId();

    String getAreaNombre();

    long getTotalCreados();

    long getResueltosEnLlamada();

    long getDerivadosAlMenosUnaVez();
}

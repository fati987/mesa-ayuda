package com.mesaayuda.metricas;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ningún porcentaje/promedio de este paquete usa double/float (regla del
 * CLAUDE.md: "no usar double ni float para nada que se sume o compare").
 * null representa "sin datos" (denominador 0) en vez de NaN o excepción.
 */
public final class PorcentajeCalculator {

    private static final int ESCALA = 2;

    private PorcentajeCalculator() {
    }

    public static BigDecimal calcular(long numerador, long denominador) {
        if (denominador <= 0) {
            return null;
        }
        return BigDecimal.valueOf(numerador)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominador), ESCALA, RoundingMode.HALF_UP);
    }
}

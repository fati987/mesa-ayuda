package com.mesaayuda.metricas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Promedio y mediana en minutos hábiles, compartido por tiempo de
 * ciclo/entrega y tiempo de espera previo. Mediana en vez de (o además
 * de) promedio para amortiguar outliers en un dataset pequeño.
 */
public final class EstadisticasMinutosCalculator {

    private static final int ESCALA = 2;

    private EstadisticasMinutosCalculator() {
    }

    public static BigDecimal promedio(List<Long> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        long suma = valores.stream().mapToLong(Long::longValue).sum();
        return BigDecimal.valueOf(suma).divide(BigDecimal.valueOf(valores.size()), ESCALA, RoundingMode.HALF_UP);
    }

    public static BigDecimal mediana(List<Long> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        List<Long> ordenado = valores.stream().sorted().toList();
        int n = ordenado.size();
        if (n % 2 == 1) {
            return BigDecimal.valueOf(ordenado.get(n / 2));
        }
        long a = ordenado.get(n / 2 - 1);
        long b = ordenado.get(n / 2);
        return BigDecimal.valueOf(a + b).divide(BigDecimal.valueOf(2), ESCALA, RoundingMode.HALF_UP);
    }
}

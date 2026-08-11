package com.mesaayuda.shared.excepcion;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        String codigoError) {
}

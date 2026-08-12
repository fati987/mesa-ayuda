package com.mesaayuda.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tipoToken,
        long expiraEnSegundos) {
}

package com.mesaayuda.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, long accessTokenExpiracionMinutos, long refreshTokenExpiracionDias) {
}

package com.mesaayuda.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.mesaayuda.usuario.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String CLAIM_TIPO = "type";
    private static final String TIPO_ACCESS = "access";
    private static final String TIPO_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(properties.accessTokenExpiracionMinutos());
        this.refreshTokenTtl = Duration.ofDays(properties.refreshTokenExpiracionDias());
    }

    public String generarAccessToken(Usuario usuario) {
        return generarToken(usuario.getCorreo(), TIPO_ACCESS, accessTokenTtl);
    }

    public String generarRefreshToken(Usuario usuario) {
        return generarToken(usuario.getCorreo(), TIPO_REFRESH, refreshTokenTtl);
    }

    private String generarToken(String correo, String tipo, Duration ttl) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(correo)
                .claim(CLAIM_TIPO, tipo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(ttl)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Lanza io.jsonwebtoken.JwtException (o subclase) si el token está mal
     * formado, mal firmado o expirado.
     */
    public Jws<Claims> parsearYValidar(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public String extraerCorreo(Jws<Claims> claims) {
        return claims.getPayload().getSubject();
    }

    public boolean esAccessToken(Jws<Claims> claims) {
        return TIPO_ACCESS.equals(claims.getPayload().get(CLAIM_TIPO, String.class));
    }

    public boolean esRefreshToken(Jws<Claims> claims) {
        return TIPO_REFRESH.equals(claims.getPayload().get(CLAIM_TIPO, String.class));
    }

    public long accessTokenExpiracionSegundos() {
        return accessTokenTtl.toSeconds();
    }
}

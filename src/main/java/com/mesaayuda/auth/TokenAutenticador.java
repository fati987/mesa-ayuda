package com.mesaayuda.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;

/**
 * Valida un access token y construye el Authentication correspondiente.
 * Punto único reusado tanto por JwtAuthenticationFilter (HTTP) como por
 * StompAuthChannelInterceptor (frame CONNECT de WebSocket), para no
 * duplicar la secuencia parsear -> validar tipo -> cargar usuario -> chequear
 * habilitado en dos lugares.
 */
@Component
public class TokenAutenticador {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public TokenAutenticador(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    /**
     * @throws JwtException             token inválido, mal firmado o expirado, o no es un access token
     * @throws UsernameNotFoundException el usuario ya no existe o está inactivo
     */
    public UsernamePasswordAuthenticationToken autenticar(String token) {
        Jws<Claims> claims = jwtService.parsearYValidar(token);
        if (!jwtService.esAccessToken(claims)) {
            throw new JwtException("El token no es un access token");
        }
        String correo = jwtService.extraerCorreo(claims);
        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(correo);
        if (!userDetails.isEnabled()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + correo);
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}

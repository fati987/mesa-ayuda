package com.mesaayuda.auth;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * El handshake HTTP de /ws no puede llevar el JWT (el navegador no manda
 * headers custom en el upgrade WebSocket) — la autenticación real de la
 * sesión STOMP pasa acá, leyendo el header nativo "Authorization" del frame
 * CONNECT, con la misma lógica que JwtAuthenticationFilter usa para HTTP
 * (TokenAutenticador). Sin un CONNECT válido, no hay suscripción posible.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final TokenAutenticador tokenAutenticador;

    public StompAuthChannelInterceptor(TokenAutenticador tokenAutenticador) {
        this.tokenAutenticador = tokenAutenticador;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith(PREFIJO_BEARER)) {
                throw new BadCredentialsException("Falta el header Authorization en el frame CONNECT");
            }
            String token = header.substring(PREFIJO_BEARER.length());
            UsernamePasswordAuthenticationToken authentication = tokenAutenticador.autenticar(token);
            accessor.setUser(authentication);
        }

        return message;
    }
}

package com.mesaayuda.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mesaayuda.auth.UsuarioPrincipal;

/**
 * Usa el correo del usuario autenticado cuando existe. "sistema" queda
 * como fallback para contextos sin autenticación (ej. el propio login).
 * El chequeo es "¿el principal es UsuarioPrincipal?", no solo
 * isAuthenticated(): el AnonymousAuthenticationFilter de Spring Security
 * deja isAuthenticated()=true con principal "anonymousUser" incluso sin token.
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
                return Optional.of(principal.getUsername());
            }
            return Optional.of("sistema");
        };
    }
}

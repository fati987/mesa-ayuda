package com.mesaayuda.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

/**
 * Placeholder hasta que exista el sprint de autenticación: no hay usuario
 * autenticado todavía, así que toda auditoría queda a nombre de "sistema".
 */
@Configuration
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("sistema");
    }
}

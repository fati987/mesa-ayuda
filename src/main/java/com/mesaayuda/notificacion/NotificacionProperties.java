package com.mesaayuda.notificacion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sla.notificacion")
public record NotificacionProperties(String remitente) {
}

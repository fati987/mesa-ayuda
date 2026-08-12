package com.mesaayuda.sla;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sla.escalamiento")
public record SlaEscalamientoProperties(int umbralMinutos, String cron) {
}

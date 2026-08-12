package com.mesaayuda.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.mesaayuda.notificacion.NotificacionProperties;
import com.mesaayuda.sla.SlaEscalamientoProperties;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({ SlaEscalamientoProperties.class, NotificacionProperties.class })
public class SchedulerConfig {
}

package com.mesaayuda.notificacion;

import java.util.List;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.usuario.Usuario;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificacionService {

    private final JavaMailSender mailSender;
    private final NotificacionProperties properties;

    public NotificacionService(JavaMailSender mailSender, NotificacionProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    /**
     * Un fallo de envío nunca debe revertir el escalamiento que ya se
     * confirmó en base de datos: se loguea acá, no se propaga.
     */
    public void notificarEscalamiento(Ticket ticket, List<Usuario> supervisores) {
        if (supervisores.isEmpty()) {
            log.warn("Ticket {} escalado sin supervisores en el área {}: no se envía notificación.",
                    ticket.getCodigo(), ticket.getAreaActual().getNombre());
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(properties.remitente());
            mensaje.setTo(supervisores.stream().map(Usuario::getCorreo).toArray(String[]::new));
            mensaje.setSubject("SLA en riesgo: ticket %s (%s)".formatted(ticket.getCodigo(), ticket.getPrioridad()));
            mensaje.setText("""
                    El ticket %s está por vencer o ya venció su SLA.

                    Estado: %s
                    Prioridad: %s
                    Área: %s
                    Vencimiento: %s
                    """.formatted(ticket.getCodigo(), ticket.getEstado(), ticket.getPrioridad(),
                    ticket.getAreaActual().getNombre(), ticket.getFechaVencimiento()));
            mailSender.send(mensaje);
        } catch (MailException ex) {
            log.error("No se pudo enviar la notificación de escalamiento del ticket {}", ticket.getCodigo(), ex);
        }
    }
}

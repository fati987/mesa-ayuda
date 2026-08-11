package com.mesaayuda.ticket.mapper;

import com.mesaayuda.contacto.dto.ContactoResumenDto;
import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.llamada.dto.LlamadaResumenDto;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResumenDto;

/**
 * Mapeo manual: los métodos acceden a relaciones lazy (areaActual,
 * categoria, contacto, llamada, usuarioCreador), así que deben invocarse
 * dentro de una transacción de lectura abierta (ver TicketService).
 */
public final class TicketMapper {

    private TicketMapper() {
    }

    public static TicketResumenDto aResumen(Ticket ticket) {
        return new TicketResumenDto(
                ticket.getCodigo(),
                ticket.getTitulo(),
                ticket.getTipo(),
                ticket.getEstado(),
                ticket.getPrioridad(),
                ticket.getUrgencia(),
                ticket.getImpacto(),
                ticket.getAreaActual().getNombre(),
                ticket.getCategoria().getNombre(),
                ticket.getContacto().getNombreCompleto(),
                ticket.isResueltoEnLlamada(),
                ticket.getCreadoEn(),
                ticket.getActualizadoEn());
    }

    public static TicketDetalleDto aDetalle(Ticket ticket) {
        return new TicketDetalleDto(
                ticket.getCodigo(),
                ticket.getTitulo(),
                ticket.getDescripcion(),
                ticket.getTipo(),
                ticket.getEstado(),
                ticket.getPrioridad(),
                ticket.getUrgencia(),
                ticket.getImpacto(),
                ticket.getOrigen(),
                ticket.getAreaActual().getNombre(),
                ticket.getCategoria().getNombre(),
                ticket.getUsuarioCreador().getNombreCompleto(),
                ticket.isResueltoEnLlamada(),
                ticket.getSolucion(),
                ticket.getMinutosPausado(),
                aContactoResumen(ticket),
                aLlamadaResumen(ticket.getLlamada()),
                ticket.getCreadoEn(),
                ticket.getActualizadoEn());
    }

    private static ContactoResumenDto aContactoResumen(Ticket ticket) {
        var contacto = ticket.getContacto();
        return new ContactoResumenDto(contacto.getNombreCompleto(), contacto.getTelefono(), contacto.getCorreo());
    }

    private static LlamadaResumenDto aLlamadaResumen(Llamada llamada) {
        if (llamada == null) {
            return null;
        }
        return new LlamadaResumenDto(
                llamada.getFechaHora(),
                llamada.getDuracionSegundos(),
                llamada.getUsuarioAtiende().getNombreCompleto());
    }
}

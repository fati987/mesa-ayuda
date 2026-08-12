package com.mesaayuda.llamada.mapper;

import com.mesaayuda.contacto.dto.ContactoResumenDto;
import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.llamada.dto.LlamadaDetalleDto;
import com.mesaayuda.llamada.dto.LlamadaResumenDto;

public final class LlamadaMapper {

    private LlamadaMapper() {
    }

    public static LlamadaResumenDto aResumen(Llamada llamada) {
        return new LlamadaResumenDto(
                llamada.getFechaHora(),
                llamada.getDuracionSegundos(),
                llamada.getUsuarioAtiende().getNombreCompleto());
    }

    public static LlamadaDetalleDto aDetalle(Llamada llamada) {
        var contacto = llamada.getContacto();
        return new LlamadaDetalleDto(
                llamada.getId(),
                aResumen(llamada),
                new ContactoResumenDto(contacto.getNombreCompleto(), contacto.getTelefono(), contacto.getCorreo()));
    }
}

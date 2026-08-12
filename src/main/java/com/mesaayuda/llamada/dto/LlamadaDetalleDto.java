package com.mesaayuda.llamada.dto;

import com.mesaayuda.contacto.dto.ContactoResumenDto;

/**
 * Expone el id interno de la llamada únicamente para que el cliente pueda
 * encadenarlo en POST /api/tickets dentro de la misma sesión de trabajo —
 * no es un identificador público de cara al contacto telefónico.
 */
public record LlamadaDetalleDto(
        Long id,
        LlamadaResumenDto llamada,
        ContactoResumenDto contacto) {
}

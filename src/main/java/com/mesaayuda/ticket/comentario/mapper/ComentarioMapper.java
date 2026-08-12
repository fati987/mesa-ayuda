package com.mesaayuda.ticket.comentario.mapper;

import com.mesaayuda.ticket.comentario.Comentario;
import com.mesaayuda.ticket.comentario.dto.ComentarioDto;

public final class ComentarioMapper {

    private ComentarioMapper() {
    }

    public static ComentarioDto aDto(Comentario comentario) {
        return new ComentarioDto(
                comentario.getUsuario().getNombreCompleto(),
                comentario.getVisibilidad(),
                comentario.getContenido(),
                comentario.getCreadoEn());
    }
}

package com.mesaayuda.usuario.mapper;

import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.dto.UsuarioDetalleDto;
import com.mesaayuda.usuario.dto.UsuarioResumenDto;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResumenDto aResumen(Usuario usuario) {
        return new UsuarioResumenDto(
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getArea().getNombre(),
                usuario.isActivo());
    }

    public static UsuarioDetalleDto aDetalle(Usuario usuario) {
        return new UsuarioDetalleDto(
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getArea().getNombre(),
                usuario.isActivo(),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn());
    }
}

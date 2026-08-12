package com.mesaayuda.auth;

import org.springframework.stereotype.Component;

import com.mesaayuda.usuario.enums.Rol;

/**
 * Regla de negocio invariable #3: un agente solo opera tickets cuya área
 * actual coincide con la suya; supervisor y admin acceden a todas.
 */
@Component
public class AccesoAreaValidator {

    public void verificarAcceso(UsuarioPrincipal usuario, Long areaId) {
        if (usuario.getRol() == Rol.AGENTE && !usuario.getAreaId().equals(areaId)) {
            throw new AccesoAreaDenegadoException();
        }
    }

    /**
     * Para listados: un agente siempre queda acotado a su propia área,
     * ignorando cualquier areaId que haya pedido. Supervisor y admin
     * reciben lo solicitado tal cual (null incluido, sin filtro).
     */
    public Long resolverFiltroArea(UsuarioPrincipal usuario, Long areaIdSolicitado) {
        return usuario.getRol() == Rol.AGENTE ? usuario.getAreaId() : areaIdSolicitado;
    }
}

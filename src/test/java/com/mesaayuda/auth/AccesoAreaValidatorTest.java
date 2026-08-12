package com.mesaayuda.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.mesaayuda.usuario.enums.Rol;

class AccesoAreaValidatorTest {

    private static final Long AREA_PROPIA = 1L;
    private static final Long OTRA_AREA = 2L;

    private final AccesoAreaValidator validator = new AccesoAreaValidator();

    private UsuarioPrincipal usuario(Rol rol, Long areaId) {
        return new UsuarioPrincipal(1L, "usuario@mesaayuda.cl", "Usuario de Prueba", rol, areaId, true);
    }

    @Test
    void agenteEnSuAreaPuedeAcceder() {
        UsuarioPrincipal agente = usuario(Rol.AGENTE, AREA_PROPIA);

        validator.verificarAcceso(agente, AREA_PROPIA);
        // no lanza excepción
    }

    @Test
    void agenteEnOtraAreaNoPuedeAcceder() {
        UsuarioPrincipal agente = usuario(Rol.AGENTE, AREA_PROPIA);

        assertThatThrownBy(() -> validator.verificarAcceso(agente, OTRA_AREA))
                .isInstanceOf(AccesoAreaDenegadoException.class);
    }

    @Test
    void supervisorAccedeACualquierArea() {
        UsuarioPrincipal supervisor = usuario(Rol.SUPERVISOR, AREA_PROPIA);

        validator.verificarAcceso(supervisor, OTRA_AREA);
        // no lanza excepción
    }

    @Test
    void adminAccedeACualquierArea() {
        UsuarioPrincipal admin = usuario(Rol.ADMIN, AREA_PROPIA);

        validator.verificarAcceso(admin, OTRA_AREA);
        // no lanza excepción
    }

    @Test
    void agenteIgnoraAreaIdSolicitadoYUsaLaPropia() {
        UsuarioPrincipal agente = usuario(Rol.AGENTE, AREA_PROPIA);

        Long areaEfectiva = validator.resolverFiltroArea(agente, OTRA_AREA);

        assertThat(areaEfectiva).isEqualTo(AREA_PROPIA);
    }

    @Test
    void supervisorConservaAreaIdSolicitado() {
        UsuarioPrincipal supervisor = usuario(Rol.SUPERVISOR, AREA_PROPIA);

        assertThat(validator.resolverFiltroArea(supervisor, OTRA_AREA)).isEqualTo(OTRA_AREA);
        assertThat(validator.resolverFiltroArea(supervisor, null)).isNull();
    }
}

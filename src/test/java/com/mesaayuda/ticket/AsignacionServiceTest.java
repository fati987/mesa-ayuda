package com.mesaayuda.ticket;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mesaayuda.area.Area;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private AsignacionService asignacionService;

    private Usuario agente;
    private Area area;

    @Test
    void permiteTomarSiEstaPorDebajoDelLimite() {
        asignacionService = new AsignacionService(ticketRepository);
        agente = agenteConId(1L);
        area = areaConLimite(3);

        when(ticketRepository.countByUsuarioAsignado_IdAndEstado(1L, EstadoTicket.EN_PROGRESO)).thenReturn(2L);

        assertThatNoException().isThrownBy(() -> asignacionService.verificarLimiteWip(agente, area));
    }

    @Test
    void rechazaTomarSiAlcanzaElLimiteExacto() {
        asignacionService = new AsignacionService(ticketRepository);
        agente = agenteConId(1L);
        area = areaConLimite(3);

        when(ticketRepository.countByUsuarioAsignado_IdAndEstado(1L, EstadoTicket.EN_PROGRESO)).thenReturn(3L);

        assertThatThrownBy(() -> asignacionService.verificarLimiteWip(agente, area))
                .isInstanceOf(LimiteWipSuperadoException.class);
    }

    private Usuario agenteConId(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Area areaConLimite(int limite) {
        Area a = new Area();
        a.setLimiteWipAgente(limite);
        return a;
    }
}

package com.mesaayuda.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.mesaayuda.area.Area;
import com.mesaayuda.auth.UsuarioPrincipal;
import com.mesaayuda.derivacion.DerivacionService;
import com.mesaayuda.sla.VencimientoService;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.UsuarioRepository;
import com.mesaayuda.usuario.enums.Rol;

@ExtendWith(MockitoExtension.class)
class TransicionServiceTest {

    private static final Long EJECUTOR_ID = 99L;

    @Mock
    private HistorialEstadoRepository historialEstadoRepository;
    @Mock
    private AsignacionService asignacionService;
    @Mock
    private DerivacionService derivacionService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VencimientoService vencimientoService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Clock clock;

    private TransicionService transicionService;
    private Area areaA;
    private Area areaB;
    private UsuarioPrincipal ejecutorPrincipal;

    @BeforeEach
    void configurar() {
        transicionService = new TransicionService(historialEstadoRepository, asignacionService, derivacionService, usuarioRepository,
                vencimientoService, eventPublisher, clock);
        areaA = area(1L, "Area A", true, 3);
        areaB = area(2L, "Area B", false, 3);
        ejecutorPrincipal = new UsuarioPrincipal(EJECUTOR_ID, "ejecutor@mesaayuda.cl", "Ejecutor", Rol.AGENTE, areaA.getId(), true);
    }

    // =====================================================================
    // Pares válidos: los 10 de la tabla del CLAUDE.md
    // =====================================================================

    @ParameterizedTest(name = "{0} -> {1} transiciona y registra historial")
    @MethodSource("paresValidos")
    void parValidoTransicionaYRegistraHistorial(EstadoTicket origen, EstadoTicket destino, Ticket ticket, ContextoTransicion contexto) {
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));

        Ticket resultado = transicionService.transicionar(ticket, destino, contexto);

        assertThat(resultado.getEstado()).isEqualTo(destino);
        verify(historialEstadoRepository).save(any(HistorialEstado.class));
    }

    static Stream<Arguments> paresValidos() {
        Area areaA = area(1L, "Area A", true, 3);
        Area areaB = area(2L, "Area B", false, 3);
        UsuarioPrincipal ejecutor = new UsuarioPrincipal(EJECUTOR_ID, "ejecutor@mesaayuda.cl", "Ejecutor", Rol.AGENTE, areaA.getId(), true);

        return Stream.of(
                Arguments.of(EstadoTicket.NUEVO, EstadoTicket.RESUELTO,
                        ticket(EstadoTicket.NUEVO, areaA, t -> t.setResueltoEnLlamada(true)),
                        ContextoTransicion.resolucion(ejecutor, null)),
                Arguments.of(EstadoTicket.NUEVO, EstadoTicket.DERIVADO,
                        ticket(EstadoTicket.NUEVO, areaA, t -> {
                        }),
                        ContextoTransicion.derivacion(ejecutor, areaB, "Requiere especialista")),
                Arguments.of(EstadoTicket.DERIVADO, EstadoTicket.EN_PROGRESO,
                        ticket(EstadoTicket.DERIVADO, areaA, t -> {
                        }),
                        ContextoTransicion.simple(ejecutor)),
                Arguments.of(EstadoTicket.DERIVADO, EstadoTicket.DERIVADO,
                        ticket(EstadoTicket.DERIVADO, areaA, t -> {
                        }),
                        ContextoTransicion.derivacion(ejecutor, areaB, "Rebote a otra área")),
                Arguments.of(EstadoTicket.EN_PROGRESO, EstadoTicket.ESPERANDO_CLIENTE,
                        ticket(EstadoTicket.EN_PROGRESO, areaA, t -> {
                        }),
                        ContextoTransicion.simple(ejecutor)),
                Arguments.of(EstadoTicket.ESPERANDO_CLIENTE, EstadoTicket.EN_PROGRESO,
                        ticket(EstadoTicket.ESPERANDO_CLIENTE, areaA, t -> t.setActualizadoEn(Instant.now().minus(10, ChronoUnit.MINUTES))),
                        ContextoTransicion.simple(ejecutor)),
                Arguments.of(EstadoTicket.EN_PROGRESO, EstadoTicket.RESUELTO,
                        ticket(EstadoTicket.EN_PROGRESO, areaA, t -> {
                        }),
                        ContextoTransicion.resolucion(ejecutor, "Se reinició el servicio")),
                Arguments.of(EstadoTicket.EN_PROGRESO, EstadoTicket.DERIVADO,
                        ticket(EstadoTicket.EN_PROGRESO, areaA, t -> t.setUsuarioAsignado(usuario(5L))),
                        ContextoTransicion.derivacion(ejecutor, areaB, "Segunda derivación")),
                Arguments.of(EstadoTicket.RESUELTO, EstadoTicket.CERRADO,
                        ticket(EstadoTicket.RESUELTO, areaA, t -> {
                        }),
                        ContextoTransicion.simple(ejecutor)),
                Arguments.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROGRESO,
                        ticket(EstadoTicket.CERRADO, areaA, t -> {
                        }),
                        ContextoTransicion.simple(ejecutor)));
    }

    // =====================================================================
    // Pares inválidos: los 26 restantes (36 pares totales - 10 válidos),
    // generados programáticamente, no listados a mano.
    // =====================================================================

    @ParameterizedTest(name = "{0} -> {1} es inválida")
    @MethodSource("paresInvalidos")
    void parInvalidoLanzaTransicionInvalida(EstadoTicket origen, EstadoTicket destino) {
        Ticket ticket = ticket(origen, areaA, t -> {
        });

        assertThatThrownBy(() -> transicionService.transicionar(ticket, destino, ContextoTransicion.simple(ejecutorPrincipal)))
                .isInstanceOf(TransicionInvalidaException.class);
    }

    static Stream<Arguments> paresInvalidos() {
        List<Arguments> invalidos = new ArrayList<>();
        for (EstadoTicket origen : EnumSet.allOf(EstadoTicket.class)) {
            for (EstadoTicket destino : EnumSet.allOf(EstadoTicket.class)) {
                if (!origen.puedeTransicionarA(destino)) {
                    invalidos.add(Arguments.of(origen, destino));
                }
            }
        }
        return invalidos.stream();
    }

    @Test
    void hayExactamente26ParesInvalidos() {
        assertThat(paresInvalidos().count()).isEqualTo(26);
    }

    // =====================================================================
    // Guardas específicas: el par es válido pero la guarda rechaza igual
    // =====================================================================

    @Test
    void nuevoAResueltoFallaSiNoFueResueltoEnLlamada() {
        Ticket ticket = ticket(EstadoTicket.NUEVO, areaA, t -> t.setResueltoEnLlamada(false));

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.RESUELTO,
                ContextoTransicion.resolucion(ejecutorPrincipal, null)))
                .isInstanceOf(TicketNoResueltoEnLlamadaException.class);
    }

    @Test
    void nuevoAResueltoFallaSiAreaNoRecibeLlamadas() {
        Ticket ticket = ticket(EstadoTicket.NUEVO, areaB, t -> t.setResueltoEnLlamada(true));

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.RESUELTO,
                ContextoTransicion.resolucion(ejecutorPrincipal, null)))
                .isInstanceOf(AreaNoRecibeLlamadasException.class);
    }

    @Test
    void nuevoADerivadoFallaSiAreaDestinoEsLaMisma() {
        Ticket ticket = ticket(EstadoTicket.NUEVO, areaA, t -> {
        });

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.DERIVADO,
                ContextoTransicion.derivacion(ejecutorPrincipal, areaA, "motivo")))
                .isInstanceOf(DerivacionInvalidaException.class);
    }

    @Test
    void nuevoADerivadoFallaSiMotivoVacio() {
        Ticket ticket = ticket(EstadoTicket.NUEVO, areaA, t -> {
        });

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.DERIVADO,
                ContextoTransicion.derivacion(ejecutorPrincipal, areaB, "  ")))
                .isInstanceOf(DerivacionInvalidaException.class);
    }

    @Test
    void derivadoAEnProgresoFallaSiAgenteNoPerteneceAlArea() {
        Ticket ticket = ticket(EstadoTicket.DERIVADO, areaB, t -> {
        });
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO,
                ContextoTransicion.simple(ejecutorPrincipal)))
                .isInstanceOf(AgenteFueraDeAreaException.class);
    }

    @Test
    void derivadoAEnProgresoFallaSiSuperaWip() {
        Ticket ticket = ticket(EstadoTicket.DERIVADO, areaA, t -> {
        });
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));
        doThrow(new LimiteWipSuperadoException(3, 3)).when(asignacionService).verificarLimiteWip(any(), any());

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO,
                ContextoTransicion.simple(ejecutorPrincipal)))
                .isInstanceOf(LimiteWipSuperadoException.class);
    }

    @Test
    void enProgresoAResueltoFallaSiSolucionVacia() {
        Ticket ticket = ticket(EstadoTicket.EN_PROGRESO, areaA, t -> {
        });
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));

        assertThatThrownBy(() -> transicionService.transicionar(ticket, EstadoTicket.RESUELTO,
                ContextoTransicion.resolucion(ejecutorPrincipal, "   ")))
                .isInstanceOf(SolucionRequeridaException.class);
    }

    // =====================================================================
    // Efectos laterales puntuales
    // =====================================================================

    @Test
    void tomaAsignaElTicketAlEjecutor() {
        Ticket ticket = ticket(EstadoTicket.DERIVADO, areaA, t -> {
        });
        when(usuarioRepository.getReferenceById(EJECUTOR_ID)).thenReturn(usuario(EJECUTOR_ID));

        transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO, ContextoTransicion.simple(ejecutorPrincipal));

        assertThat(ticket.getUsuarioAsignado()).isNotNull();
        assertThat(ticket.getUsuarioAsignado().getId()).isEqualTo(EJECUTOR_ID);
    }

    @Test
    void segundaDerivacionLimpiaElAgenteAsignado() {
        Ticket ticket = ticket(EstadoTicket.EN_PROGRESO, areaA, t -> t.setUsuarioAsignado(usuario(5L)));
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));

        transicionService.transicionar(ticket, EstadoTicket.DERIVADO,
                ContextoTransicion.derivacion(ejecutorPrincipal, areaB, "motivo"));

        assertThat(ticket.getUsuarioAsignado()).isNull();
    }

    @Test
    void reanudacionAcumulaMinutosPausados() {
        Ticket ticket = ticket(EstadoTicket.ESPERANDO_CLIENTE, areaA, t -> {
        });
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));
        when(vencimientoService.minutosHabilesEntre(any(), any())).thenReturn(15L);

        transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO, ContextoTransicion.simple(ejecutorPrincipal));

        assertThat(ticket.getMinutosPausado()).isEqualTo(15);
    }

    // =====================================================================
    // Vencimiento SLA (Sprint 4)
    // =====================================================================

    @Test
    void primeraDerivacionCalculaFechaVencimiento() {
        Ticket ticket = ticket(EstadoTicket.NUEVO, areaA, t -> t.setPrioridad(Prioridad.CRITICA));
        Instant esperado = Instant.parse("2026-03-10T13:00:00Z");
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));
        when(clock.instant()).thenReturn(Instant.parse("2026-03-06T18:00:00Z"));
        when(vencimientoService.calcularVencimientoDerivacion(any(), any())).thenReturn(esperado);

        transicionService.transicionar(ticket, EstadoTicket.DERIVADO, ContextoTransicion.derivacion(ejecutorPrincipal, areaB, "motivo"));

        assertThat(ticket.getFechaVencimiento()).isEqualTo(esperado);
    }

    @Test
    void reboteNoRecalculaFechaVencimiento() {
        Instant fechaExistente = Instant.parse("2026-03-10T13:00:00Z");
        Ticket ticket = ticket(EstadoTicket.DERIVADO, areaA, t -> t.setFechaVencimiento(fechaExistente));
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));

        transicionService.transicionar(ticket, EstadoTicket.DERIVADO, ContextoTransicion.derivacion(ejecutorPrincipal, areaB, "rebote"));

        assertThat(ticket.getFechaVencimiento()).isEqualTo(fechaExistente);
        verify(vencimientoService, never()).calcularVencimientoDerivacion(any(), any());
    }

    @Test
    void segundaDerivacionNoRecalculaFechaVencimiento() {
        Instant fechaExistente = Instant.parse("2026-03-10T13:00:00Z");
        Ticket ticket = ticket(EstadoTicket.EN_PROGRESO, areaA, t -> t.setFechaVencimiento(fechaExistente));
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));

        transicionService.transicionar(ticket, EstadoTicket.DERIVADO,
                ContextoTransicion.derivacion(ejecutorPrincipal, areaB, "segunda derivacion"));

        assertThat(ticket.getFechaVencimiento()).isEqualTo(fechaExistente);
        verify(vencimientoService, never()).calcularVencimientoDerivacion(any(), any());
    }

    @Test
    void reanudacionCorreFechaVencimientoPorMinutosHabiles() {
        Instant fechaExistente = Instant.parse("2026-03-10T13:00:00Z");
        Ticket ticket = ticket(EstadoTicket.ESPERANDO_CLIENTE, areaA, t -> t.setFechaVencimiento(fechaExistente));
        when(usuarioRepository.getReferenceById(anyLong())).thenReturn(usuario(EJECUTOR_ID));
        when(vencimientoService.minutosHabilesEntre(any(), any())).thenReturn(20L);

        transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO, ContextoTransicion.simple(ejecutorPrincipal));

        assertThat(ticket.getFechaVencimiento()).isEqualTo(fechaExistente.plus(20, ChronoUnit.MINUTES));
        assertThat(ticket.getMinutosPausado()).isEqualTo(20);
    }

    // =====================================================================
    // Fixtures
    // =====================================================================

    private static Ticket ticket(EstadoTicket estado, Area areaActual, java.util.function.Consumer<Ticket> personalizar) {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setEstado(estado);
        ticket.setAreaActual(areaActual);
        ticket.setResueltoEnLlamada(false);
        ticket.setMinutosPausado(0);
        ticket.setActualizadoEn(Instant.now());
        personalizar.accept(ticket);
        return ticket;
    }

    private static Area area(Long id, String nombre, boolean recibeLlamadas, int limiteWip) {
        Area area = new Area();
        area.setId(id);
        area.setNombre(nombre);
        area.setRecibeLlamadas(recibeLlamadas);
        area.setLimiteWipAgente(limiteWip);
        return area;
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreCompleto("Usuario " + id);
        return usuario;
    }
}

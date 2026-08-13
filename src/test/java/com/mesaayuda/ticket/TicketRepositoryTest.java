package com.mesaayuda.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.categoria.Categoria;
import com.mesaayuda.categoria.CategoriaRepository;
import com.mesaayuda.contacto.Contacto;
import com.mesaayuda.contacto.ContactoRepository;
import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.llamada.LlamadaRepository;
import com.mesaayuda.testsoporte.PostgresTestcontainer;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Origen;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;
import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.UsuarioRepository;
import com.mesaayuda.usuario.enums.Rol;

/**
 * Datos armados a mano en cada test (no depende del seed de Flyway V2) para
 * tener control total sobre lo que se está afirmando. Como el container
 * Postgres es un singleton compartido por toda la corrida (patrón de
 * PostgresTestcontainer), cada test usa su propia Area con nombre único
 * como ancla de aislamiento: filtrar por areaId propio garantiza que ningún
 * otro test (de esta clase u otra) contamine el conteo.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class TicketRepositoryTest extends PostgresTestcontainer {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ContactoRepository contactoRepository;
    @Autowired
    private LlamadaRepository llamadaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // =====================================================================
    // buscarParaTablero: filtra por área + estado, excluye siempre los
    // resueltos en llamada (regla invariable #10) y los eliminados.
    // =====================================================================

    @Test
    void buscarParaTableroExcluyeResueltosEnLlamadaYOtraAreaYOtroEstado() {
        Area areaA = crearArea("Area RepoTablero A");
        Area areaB = crearArea("Area RepoTablero B");
        Categoria categoria = crearCategoria("Categoria RepoTablero");
        Usuario creador = crearUsuario("creador.repotablero@mesaayuda.cl", areaA);
        Contacto contacto = crearContacto("+56900000001");
        Llamada llamada = crearLlamada(contacto, creador);

        Ticket enAreaAResueltoEnLlamada = crearTicket("TAB-0001", areaA, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, true);
        Ticket enAreaANoResueltoEnLlamada = crearTicket("TAB-0002", areaA, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, false);
        crearTicket("TAB-0003", areaB, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, false);
        crearTicket("TAB-0004", areaA, categoria, contacto, creador, llamada,
                EstadoTicket.DERIVADO, Prioridad.MEDIA, false);

        Page<Ticket> resultado = ticketRepository.buscarParaTablero(areaA.getId(), EstadoTicket.EN_PROGRESO, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent()).extracting(Ticket::getCodigo).containsExactly(enAreaANoResueltoEnLlamada.getCodigo());
        assertThat(resultado.getContent().get(0).isResueltoEnLlamada()).isFalse();
        // sanity: el descartado por resuelto_en_llamada existe pero no aparece
        assertThat(enAreaAResueltoEnLlamada.isResueltoEnLlamada()).isTrue();
    }

    // =====================================================================
    // buscar: estado, prioridad y areaId son filtros opcionales
    // independientes ("(:param is null or ...)").
    // =====================================================================

    @Test
    void buscarAplicaCadaFiltroDeFormaOpcional() {
        Area area = crearArea("Area RepoBuscar Unica");
        Categoria categoria = crearCategoria("Categoria RepoBuscar");
        Usuario creador = crearUsuario("creador.repobuscar@mesaayuda.cl", area);
        Contacto contacto = crearContacto("+56900000002");
        Llamada llamada = crearLlamada(contacto, creador);

        Ticket enProgresoAlta = crearTicket("BUS-0001", area, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.ALTA, false);
        Ticket enProgresoMedia = crearTicket("BUS-0002", area, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, false);
        Ticket derivadoAlta = crearTicket("BUS-0003", area, categoria, contacto, creador, llamada,
                EstadoTicket.DERIVADO, Prioridad.ALTA, false);
        Ticket eliminado = crearTicket("BUS-0004", area, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.ALTA, false);
        eliminado.setEliminadoEn(Instant.now());
        ticketRepository.save(eliminado);

        // Todos null: devuelve todos los no eliminados (chequeo por
        // contención, no por total exacto, porque el container es
        // compartido con el resto de la suite y el seed de Flyway).
        Page<Ticket> sinFiltros = ticketRepository.buscar(null, null, null, PageRequest.of(0, 200));
        List<String> codigosSinFiltros = codigos(sinFiltros);
        assertThat(codigosSinFiltros).contains(
                enProgresoAlta.getCodigo(), enProgresoMedia.getCodigo(), derivadoAlta.getCodigo());
        assertThat(codigosSinFiltros).doesNotContain(eliminado.getCodigo());

        // Solo estado seteado: presencia/ausencia por código, no por total,
        // por la misma razón de aislamiento.
        Page<Ticket> soloEstado = ticketRepository.buscar(EstadoTicket.DERIVADO, null, null, PageRequest.of(0, 200));
        List<String> codigosSoloEstado = codigos(soloEstado);
        assertThat(codigosSoloEstado).contains(derivadoAlta.getCodigo());
        assertThat(codigosSoloEstado).doesNotContain(enProgresoAlta.getCodigo(), enProgresoMedia.getCodigo(), eliminado.getCodigo());

        // Los 3 filtros a la vez, acotados a mi área única: conteo exacto seguro.
        Page<Ticket> los3 = ticketRepository.buscar(EstadoTicket.EN_PROGRESO, Prioridad.ALTA, area.getId(), PageRequest.of(0, 10));
        assertThat(los3.getTotalElements()).isEqualTo(1);
        assertThat(los3.getContent().get(0).getCodigo()).isEqualTo(enProgresoAlta.getCodigo());

        // Combinación que no matchea nada, acotada a mi área única: página vacía.
        Page<Ticket> sinMatch = ticketRepository.buscar(EstadoTicket.RESUELTO, Prioridad.BAJA, area.getId(), PageRequest.of(0, 10));
        assertThat(sinMatch.getTotalElements()).isEqualTo(0);
        assertThat(sinMatch.getContent()).isEmpty();
    }

    private List<String> codigos(Page<Ticket> pagina) {
        return pagina.getContent().stream().map(Ticket::getCodigo).collect(Collectors.toList());
    }

    // =====================================================================
    // Fixtures
    // =====================================================================

    private Area crearArea(String nombre) {
        Area area = new Area();
        area.setNombre(nombre);
        area.setRecibeLlamadas(true);
        area.setLimiteWipAgente(5);
        area.setActivo(true);
        return areaRepository.save(area);
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion("Categoria de prueba");
        categoria.setActivo(true);
        return categoriaRepository.save(categoria);
    }

    private Contacto crearContacto(String telefono) {
        Contacto contacto = new Contacto();
        contacto.setNombreCompleto("Contacto Repo Test");
        contacto.setTelefono(telefono);
        contacto.setCorreo(null);
        contacto.setActivo(true);
        return contactoRepository.save(contacto);
    }

    private Usuario crearUsuario(String correo, Area area) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Usuario Repo Test");
        usuario.setCorreo(correo);
        usuario.setRol(Rol.AGENTE);
        usuario.setArea(area);
        usuario.setContrasenaHash("hash-no-usado-en-este-test");
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Llamada crearLlamada(Contacto contacto, Usuario atiende) {
        Llamada llamada = new Llamada();
        llamada.setContacto(contacto);
        llamada.setUsuarioAtiende(atiende);
        llamada.setFechaHora(Instant.now());
        llamada.setDuracionSegundos(120);
        return llamadaRepository.save(llamada);
    }

    private Ticket crearTicket(String codigo, Area area, Categoria categoria, Contacto contacto, Usuario creador,
            Llamada llamada, EstadoTicket estado, Prioridad prioridad, boolean resueltoEnLlamada) {
        Ticket ticket = new Ticket();
        ticket.setCodigo(codigo);
        ticket.setLlamada(llamada);
        ticket.setContacto(contacto);
        ticket.setAreaActual(area);
        ticket.setCategoria(categoria);
        ticket.setUsuarioCreador(creador);
        ticket.setTipo(TipoTicket.INCIDENTE);
        ticket.setEstado(estado);
        ticket.setPrioridad(prioridad);
        ticket.setUrgencia(Urgencia.MEDIA);
        ticket.setImpacto(Impacto.INDIVIDUAL);
        ticket.setOrigen(Origen.TELEFONO);
        ticket.setTitulo("Ticket de prueba " + codigo);
        ticket.setDescripcion("Descripcion de prueba para " + codigo);
        ticket.setResueltoEnLlamada(resueltoEnLlamada);
        ticket.setMinutosPausado(0);
        ticket.setEscalado(false);
        return ticketRepository.save(ticket);
    }
}

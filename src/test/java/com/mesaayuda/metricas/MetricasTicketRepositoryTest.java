package com.mesaayuda.metricas;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.categoria.Categoria;
import com.mesaayuda.categoria.CategoriaRepository;
import com.mesaayuda.contacto.Contacto;
import com.mesaayuda.contacto.ContactoRepository;
import com.mesaayuda.derivacion.Derivacion;
import com.mesaayuda.derivacion.DerivacionRepository;
import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.llamada.LlamadaRepository;
import com.mesaayuda.metricas.proyeccion.CfdPuntoProjection;
import com.mesaayuda.metricas.proyeccion.DerivacionOrdinalProjection;
import com.mesaayuda.metricas.proyeccion.ResumenAreaCreacionProjection;
import com.mesaayuda.testsoporte.PostgresTestcontainer;
import com.mesaayuda.ticket.HistorialEstado;
import com.mesaayuda.ticket.HistorialEstadoRepository;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.TicketRepository;
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
 * Cubre las 3 consultas nativas de mayor riesgo de MetricasTicketRepository
 * (LATERAL, generate_series, ROW_NUMBER) con dataset mínimo armado a mano,
 * para poder afirmar valores EXACTOS (a diferencia de los *IntegrationTest
 * migrados, que son por invariante sobre el seed completo).
 *
 * creadoEn de Ticket/HistorialEstado/Derivacion lo pisa el
 * AuditingEntityListener al persistir (siempre a "ahora"), así que estos
 * tests usan JdbcTemplate para reescribir esas columnas directamente después
 * del insert vía JPA — es la única forma de controlar fechas pasadas
 * exactas sin tocar el reloj de la aplicación (Clock.systemUTC(), sin
 * override en este proyecto).
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MetricasTicketRepositoryTest extends PostgresTestcontainer {

    @Autowired
    private MetricasTicketRepository metricasTicketRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private HistorialEstadoRepository historialEstadoRepository;
    @Autowired
    private DerivacionRepository derivacionRepository;
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
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =====================================================================
    // flujoAcumulado: estado vigente de cada ticket al cierre de cada día
    // ("cierre" = creado_en <= (dia+1) en la zona dada, inclusive).
    // Días sintéticos lejos en el pasado (-40/-38/-36) para no chocar con
    // el seed de Flyway V2 (fechas fijas de febrero 2026) ni con datos de
    // "ahora" que generan otros tests en el mismo container compartido.
    // =====================================================================

    @Test
    void flujoAcumuladoReconstruyeElEstadoVigenteAlCierreDeCadaDia() {
        Area area = crearArea("Area CFD Test");
        Categoria categoria = crearCategoria("Categoria CFD Test");
        Usuario creador = crearUsuario("creador.cfd@mesaayuda.cl", area);
        Contacto contacto = crearContacto("+56900000010");
        Llamada llamada = crearLlamada(contacto, creador);

        LocalDate dia0 = LocalDate.now(ZoneOffset.UTC).minusDays(40);
        LocalDate dia1 = dia0.plusDays(2);
        LocalDate dia2 = dia0.plusDays(4);

        Instant creacion = dia0.atTime(12, 0).toInstant(ZoneOffset.UTC);
        Instant derivadoEn = dia1.atTime(10, 0).toInstant(ZoneOffset.UTC);
        Instant enProgresoEn = dia2.atTime(15, 0).toInstant(ZoneOffset.UTC);

        Ticket ticket = crearTicket("CFD-0001", area, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, false);
        actualizarCreadoEn("ticket", ticket.getId(), creacion);

        HistorialEstado nuevo = guardarHistorial(ticket, null, EstadoTicket.NUEVO, creador);
        actualizarCreadoEn("historial_estado", nuevo.getId(), creacion);
        HistorialEstado derivado = guardarHistorial(ticket, EstadoTicket.NUEVO, EstadoTicket.DERIVADO, creador);
        actualizarCreadoEn("historial_estado", derivado.getId(), derivadoEn);
        HistorialEstado enProgreso = guardarHistorial(ticket, EstadoTicket.DERIVADO, EstadoTicket.EN_PROGRESO, creador);
        actualizarCreadoEn("historial_estado", enProgreso.getId(), enProgresoEn);

        // el rango arranca 2 días antes de la creación para poder afirmar
        // que el ticket no aparece en ningún punto anterior a su alta
        // (t.creado_en <= (dia+1) todavía no se cumple ahí).
        LocalDate desdeRango = dia0.minusDays(2);
        List<CfdPuntoProjection> puntos = metricasTicketRepository.flujoAcumulado(desdeRango, dia2, "UTC");

        assertThat(puntos.stream().anyMatch(p -> p.getDia().equals(dia0.minusDays(1)))).isFalse();
        assertThat(puntos.stream().anyMatch(p -> p.getDia().equals(dia0.minusDays(2)))).isFalse();

        assertThat(puntoDelDia(puntos, dia0)).isEqualTo(EstadoTicket.NUEVO);
        assertThat(puntoDelDia(puntos, dia0.plusDays(1))).isEqualTo(EstadoTicket.NUEVO);
        assertThat(puntoDelDia(puntos, dia1)).isEqualTo(EstadoTicket.DERIVADO);
        assertThat(puntoDelDia(puntos, dia1.plusDays(1))).isEqualTo(EstadoTicket.DERIVADO);
        assertThat(puntoDelDia(puntos, dia2)).isEqualTo(EstadoTicket.EN_PROGRESO);
    }

    private EstadoTicket puntoDelDia(List<CfdPuntoProjection> puntos, LocalDate dia) {
        List<CfdPuntoProjection> delDia = puntos.stream().filter(p -> p.getDia().equals(dia)).toList();
        assertThat(delDia).hasSize(1);
        assertThat(delDia.get(0).getCantidad()).isEqualTo(1L);
        return delDia.get(0).getEstado();
    }

    // =====================================================================
    // derivacionesOrdinales: ROW_NUMBER() over (partition by ticket_id
    // order by creado_en) — la posición ordinal sigue el orden temporal de
    // inserción, no el orden en que aparecen en la tabla.
    // =====================================================================

    @Test
    void derivacionesOrdinalesNumeraEnOrdenDeInsercion() {
        Area areaA = crearArea("Area Ordinal A");
        Area areaB = crearArea("Area Ordinal B");
        Area areaC = crearArea("Area Ordinal C");
        Categoria categoria = crearCategoria("Categoria Ordinal Test");
        Usuario creador = crearUsuario("creador.ordinal@mesaayuda.cl", areaA);
        Contacto contacto = crearContacto("+56900000011");
        Llamada llamada = crearLlamada(contacto, creador);

        Ticket ticket = crearTicket("ORD-0001", areaC, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, false);

        Derivacion primera = guardarDerivacion(ticket, areaA, areaB, creador, "Primera derivación");
        actualizarCreadoEn("derivacion", primera.getId(), Instant.now().minusSeconds(120));
        Derivacion segunda = guardarDerivacion(ticket, areaB, areaC, creador, "Segunda derivación");
        actualizarCreadoEn("derivacion", segunda.getId(), Instant.now().minusSeconds(60));

        List<DerivacionOrdinalProjection> resultado = metricasTicketRepository.derivacionesOrdinales(List.of(ticket.getId()));

        assertThat(resultado).hasSize(2);
        DerivacionOrdinalProjection pos1 = resultado.stream().filter(p -> p.getPosicion() == 1).findFirst().orElseThrow();
        DerivacionOrdinalProjection pos2 = resultado.stream().filter(p -> p.getPosicion() == 2).findFirst().orElseThrow();

        assertThat(pos1.getTicketId()).isEqualTo(ticket.getId());
        assertThat(pos1.getAreaDestinoId()).isEqualTo(areaB.getId());
        assertThat(pos2.getTicketId()).isEqualTo(ticket.getId());
        assertThat(pos2.getAreaDestinoId()).isEqualTo(areaC.getId());
    }

    // =====================================================================
    // resumenPorAreaCreacion: área de creación = área origen de la primera
    // Derivacion, o el área actual si el ticket nunca se derivó. Aislado
    // por areaId propio (único), no por ventana de tiempo: la consulta no
    // filtra por área, así que solo así se garantiza determinismo frente al
    // resto de la suite.
    // =====================================================================

    @Test
    void resumenPorAreaCreacionResuelveAreaOrigenParaDerivadosYAreaActualParaElResto() {
        Area areaNuncaDerivado = crearArea("Area Resumen Nunca Derivado");
        Area areaOrigen = crearArea("Area Resumen Origen");
        Area areaDestinoActual = crearArea("Area Resumen Destino Actual");
        Categoria categoria = crearCategoria("Categoria Resumen Test");
        Usuario creador = crearUsuario("creador.resumen@mesaayuda.cl", areaNuncaDerivado);
        Contacto contacto = crearContacto("+56900000012");
        Llamada llamada = crearLlamada(contacto, creador);

        Ticket nuncaDerivado = crearTicket("RES-0001", areaNuncaDerivado, categoria, contacto, creador, llamada,
                EstadoTicket.RESUELTO, Prioridad.MEDIA, true);

        Ticket derivadoUnaVez = crearTicket("RES-0002", areaDestinoActual, categoria, contacto, creador, llamada,
                EstadoTicket.EN_PROGRESO, Prioridad.MEDIA, false);
        guardarDerivacion(derivadoUnaVez, areaOrigen, areaDestinoActual, creador, "Derivación única");

        Instant desde = Instant.parse("2000-01-01T00:00:00Z");
        Instant hasta = Instant.now().plusSeconds(3600);
        List<ResumenAreaCreacionProjection> resultado = metricasTicketRepository.resumenPorAreaCreacion(desde, hasta);

        ResumenAreaCreacionProjection filaNuncaDerivado = filaDeArea(resultado, areaNuncaDerivado.getId());
        assertThat(filaNuncaDerivado.getAreaId()).isEqualTo(areaNuncaDerivado.getId());
        assertThat(filaNuncaDerivado.getTotalCreados()).isEqualTo(1L);
        assertThat(filaNuncaDerivado.getResueltosEnLlamada()).isEqualTo(1L);
        assertThat(filaNuncaDerivado.getDerivadosAlMenosUnaVez()).isEqualTo(0L);

        ResumenAreaCreacionProjection filaOrigen = filaDeArea(resultado, areaOrigen.getId());
        assertThat(filaOrigen.getAreaId()).isEqualTo(areaOrigen.getId());
        assertThat(filaOrigen.getTotalCreados()).isEqualTo(1L);
        assertThat(filaOrigen.getResueltosEnLlamada()).isEqualTo(0L);
        assertThat(filaOrigen.getDerivadosAlMenosUnaVez()).isEqualTo(1L);

        // el ticket derivado NO debe aparecer agrupado bajo su área actual
        // (areaDestinoActual): su área de creación es la de origen.
        assertThat(resultado.stream().noneMatch(f -> f.getAreaId().equals(areaDestinoActual.getId()))).isTrue();
    }

    private ResumenAreaCreacionProjection filaDeArea(List<ResumenAreaCreacionProjection> filas, Long areaId) {
        return filas.stream().filter(f -> f.getAreaId().equals(areaId)).findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró fila para areaId=" + areaId));
    }

    // =====================================================================
    // Fixtures
    // =====================================================================

    private void actualizarCreadoEn(String tabla, Long id, Instant instante) {
        jdbcTemplate.update("update " + tabla + " set creado_en = ? where id = ?", Timestamp.from(instante), id);
    }

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
        contacto.setNombreCompleto("Contacto Metricas Test");
        contacto.setTelefono(telefono);
        contacto.setCorreo(null);
        contacto.setActivo(true);
        return contactoRepository.save(contacto);
    }

    private Usuario crearUsuario(String correo, Area area) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Usuario Metricas Test");
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

    private HistorialEstado guardarHistorial(Ticket ticket, EstadoTicket anterior, EstadoTicket nuevo, Usuario usuario) {
        HistorialEstado historial = new HistorialEstado();
        historial.setTicket(ticket);
        historial.setEstadoAnterior(anterior);
        historial.setEstadoNuevo(nuevo);
        historial.setUsuario(usuario);
        historial.setComentario("Transición de prueba");
        return historialEstadoRepository.save(historial);
    }

    private Derivacion guardarDerivacion(Ticket ticket, Area origen, Area destino, Usuario usuarioDeriva, String motivo) {
        Derivacion derivacion = new Derivacion();
        derivacion.setTicket(ticket);
        derivacion.setAreaOrigen(origen);
        derivacion.setAreaDestino(destino);
        derivacion.setUsuarioDeriva(usuarioDeriva);
        derivacion.setMotivo(motivo);
        return derivacionRepository.save(derivacion);
    }
}

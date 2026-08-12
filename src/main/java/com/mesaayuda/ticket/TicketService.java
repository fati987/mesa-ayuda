package com.mesaayuda.ticket;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaNoEncontradaException;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.auth.AccesoAreaValidator;
import com.mesaayuda.auth.UsuarioActualProvider;
import com.mesaayuda.auth.UsuarioPrincipal;
import com.mesaayuda.categoria.Categoria;
import com.mesaayuda.categoria.CategoriaNoEncontradaException;
import com.mesaayuda.categoria.CategoriaRepository;
import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.llamada.LlamadaNoEncontradaException;
import com.mesaayuda.llamada.LlamadaRepository;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.dto.TicketCrearRequest;
import com.mesaayuda.ticket.dto.TicketDerivarRequest;
import com.mesaayuda.ticket.dto.TicketDetalleDto;
import com.mesaayuda.ticket.dto.TicketResolverRequest;
import com.mesaayuda.ticket.dto.TicketResumenDto;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Origen;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.mapper.TicketMapper;
import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final LlamadaRepository llamadaRepository;
    private final AreaRepository areaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActualProvider usuarioActualProvider;
    private final AccesoAreaValidator accesoAreaValidator;
    private final TransicionService transicionService;
    private final TicketCodigoGenerator ticketCodigoGenerator;

    public TicketService(TicketRepository ticketRepository, HistorialEstadoRepository historialEstadoRepository,
            LlamadaRepository llamadaRepository, AreaRepository areaRepository, CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository, UsuarioActualProvider usuarioActualProvider,
            AccesoAreaValidator accesoAreaValidator, TransicionService transicionService,
            TicketCodigoGenerator ticketCodigoGenerator) {
        this.ticketRepository = ticketRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.llamadaRepository = llamadaRepository;
        this.areaRepository = areaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActualProvider = usuarioActualProvider;
        this.accesoAreaValidator = accesoAreaValidator;
        this.transicionService = transicionService;
        this.ticketCodigoGenerator = ticketCodigoGenerator;
    }

    public PaginaResponse<TicketResumenDto> listar(EstadoTicket estado, Prioridad prioridad, Long areaId, Pageable pageable) {
        UsuarioPrincipal actual = usuarioActualProvider.actual();
        Long areaEfectiva = accesoAreaValidator.resolverFiltroArea(actual, areaId);
        return PaginaResponse.de(ticketRepository.buscar(estado, prioridad, areaEfectiva, pageable), TicketMapper::aResumen);
    }

    public TicketDetalleDto obtenerPorCodigo(String codigo) {
        Ticket ticket = ticketRepository.findByCodigoAndEliminadoEnIsNull(codigo)
                .orElseThrow(() -> new TicketNoEncontradoException(codigo));
        accesoAreaValidator.verificarAcceso(usuarioActualProvider.actual(), ticket.getAreaActual().getId());
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto crear(TicketCrearRequest request) {
        Llamada llamada = llamadaRepository.findById(request.llamadaId())
                .orElseThrow(() -> new LlamadaNoEncontradaException(request.llamadaId()));
        Area area = areaRepository.findById(request.areaId())
                .orElseThrow(() -> new AreaNoEncontradaException(request.areaId()));
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException(request.categoriaId()));

        // Regla invariable #1: solo áreas con recibeLlamadas pueden crear
        // tickets de origen telefónico.
        if (!area.isRecibeLlamadas()) {
            throw new AreaNoRecibeLlamadasException(area.getNombre());
        }

        Usuario creador = usuarioRepository.getReferenceById(usuarioActualProvider.actual().getId());

        Ticket ticket = new Ticket();
        ticket.setCodigo(ticketCodigoGenerator.generar());
        ticket.setLlamada(llamada);
        ticket.setContacto(llamada.getContacto());
        ticket.setAreaActual(area);
        ticket.setCategoria(categoria);
        ticket.setUsuarioCreador(creador);
        ticket.setTipo(request.tipo());
        ticket.setEstado(EstadoTicket.NUEVO);
        ticket.setPrioridad(request.prioridad());
        ticket.setUrgencia(request.urgencia());
        ticket.setImpacto(request.impacto());
        ticket.setOrigen(Origen.TELEFONO);
        ticket.setTitulo(request.titulo());
        ticket.setDescripcion(request.descripcion());
        ticket.setResueltoEnLlamada(request.resueltoEnLlamada());
        ticket.setSolucion(request.solucion());
        ticket.setMinutosPausado(0);
        ticket = ticketRepository.save(ticket);

        // NUEVO es el estado inicial, no una transición: se registra el
        // historial directo, sin pasar por TransicionService (que valida
        // pares origen->destino del grafo).
        HistorialEstado historialInicial = new HistorialEstado();
        historialInicial.setTicket(ticket);
        historialInicial.setEstadoAnterior(null);
        historialInicial.setEstadoNuevo(EstadoTicket.NUEVO);
        historialInicial.setUsuario(creador);
        historialInicial.setComentario("Ticket creado durante la llamada.");
        historialEstadoRepository.save(historialInicial);

        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto derivar(String codigo, TicketDerivarRequest request) {
        Ticket ticket = cargarConAcceso(codigo);
        Area areaDestino = areaRepository.findById(request.areaDestinoId())
                .orElseThrow(() -> new AreaNoEncontradaException(request.areaDestinoId()));
        transicionService.transicionar(ticket, EstadoTicket.DERIVADO,
                ContextoTransicion.derivacion(usuarioActualProvider.actual(), areaDestino, request.motivo()));
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto tomar(String codigo) {
        Ticket ticket = cargarConAcceso(codigo);
        transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO, ContextoTransicion.simple(usuarioActualProvider.actual()));
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto resolver(String codigo, TicketResolverRequest request) {
        Ticket ticket = cargarConAcceso(codigo);
        transicionService.transicionar(ticket, EstadoTicket.RESUELTO,
                ContextoTransicion.resolucion(usuarioActualProvider.actual(), request.solucion()));
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto pausar(String codigo) {
        Ticket ticket = cargarConAcceso(codigo);
        transicionService.transicionar(ticket, EstadoTicket.ESPERANDO_CLIENTE, ContextoTransicion.simple(usuarioActualProvider.actual()));
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto reanudar(String codigo) {
        Ticket ticket = cargarConAcceso(codigo);
        transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO, ContextoTransicion.simple(usuarioActualProvider.actual()));
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto cerrar(String codigo) {
        Ticket ticket = cargarConAcceso(codigo);
        transicionService.transicionar(ticket, EstadoTicket.CERRADO, ContextoTransicion.simple(usuarioActualProvider.actual()));
        return aDetalleConHistorial(ticket);
    }

    @Transactional
    public TicketDetalleDto reabrir(String codigo) {
        Ticket ticket = cargarConAcceso(codigo);
        transicionService.transicionar(ticket, EstadoTicket.EN_PROGRESO, ContextoTransicion.simple(usuarioActualProvider.actual()));
        return aDetalleConHistorial(ticket);
    }

    private Ticket cargarConAcceso(String codigo) {
        Ticket ticket = ticketRepository.findByCodigoAndEliminadoEnIsNull(codigo)
                .orElseThrow(() -> new TicketNoEncontradoException(codigo));
        accesoAreaValidator.verificarAcceso(usuarioActualProvider.actual(), ticket.getAreaActual().getId());
        return ticket;
    }

    private TicketDetalleDto aDetalleConHistorial(Ticket ticket) {
        List<HistorialEstado> historial = historialEstadoRepository.findByTicketIdOrderByCreadoEnAsc(ticket.getId());
        return TicketMapper.aDetalle(ticket, historial);
    }
}

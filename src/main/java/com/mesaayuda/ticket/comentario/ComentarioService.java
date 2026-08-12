package com.mesaayuda.ticket.comentario;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.auth.AccesoAreaValidator;
import com.mesaayuda.auth.UsuarioActualProvider;
import com.mesaayuda.auth.UsuarioPrincipal;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.ticket.TicketNoEncontradoException;
import com.mesaayuda.ticket.TicketRepository;
import com.mesaayuda.ticket.comentario.dto.ComentarioCrearRequest;
import com.mesaayuda.ticket.comentario.dto.ComentarioDto;
import com.mesaayuda.ticket.comentario.mapper.ComentarioMapper;
import com.mesaayuda.ticket.enums.Visibilidad;
import com.mesaayuda.usuario.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActualProvider usuarioActualProvider;
    private final AccesoAreaValidator accesoAreaValidator;

    public ComentarioService(ComentarioRepository comentarioRepository, TicketRepository ticketRepository,
            UsuarioRepository usuarioRepository, UsuarioActualProvider usuarioActualProvider,
            AccesoAreaValidator accesoAreaValidator) {
        this.comentarioRepository = comentarioRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActualProvider = usuarioActualProvider;
        this.accesoAreaValidator = accesoAreaValidator;
    }

    /**
     * Hoy cualquier rol interno autenticado ve PUBLICO+INTERNO: no existe
     * todavía ningún consumidor externo (el Contacto nunca se autentica).
     * Punto de extensión explícito para cuando exista uno: devolvería solo
     * PUBLICO para ese consumidor.
     */
    public List<Visibilidad> visibilidadesPermitidas(UsuarioPrincipal actual) {
        return List.of(Visibilidad.PUBLICO, Visibilidad.INTERNO);
    }

    public PaginaResponse<ComentarioDto> listar(String codigoTicket, Pageable pageable) {
        Ticket ticket = cargarConAcceso(codigoTicket);
        UsuarioPrincipal actual = usuarioActualProvider.actual();
        var pagina = comentarioRepository.findByTicketIdAndVisibilidadInOrderByCreadoEnAsc(
                ticket.getId(), visibilidadesPermitidas(actual), pageable);
        return PaginaResponse.de(pagina, ComentarioMapper::aDto);
    }

    @Transactional
    public ComentarioDto crear(String codigoTicket, ComentarioCrearRequest request) {
        Ticket ticket = cargarConAcceso(codigoTicket);
        UsuarioPrincipal actual = usuarioActualProvider.actual();

        Comentario comentario = new Comentario();
        comentario.setTicket(ticket);
        comentario.setUsuario(usuarioRepository.getReferenceById(actual.getId()));
        comentario.setVisibilidad(request.visibilidad());
        comentario.setContenido(request.contenido());

        return ComentarioMapper.aDto(comentarioRepository.save(comentario));
    }

    private Ticket cargarConAcceso(String codigo) {
        Ticket ticket = ticketRepository.findByCodigoAndEliminadoEnIsNull(codigo)
                .orElseThrow(() -> new TicketNoEncontradoException(codigo));
        accesoAreaValidator.verificarAcceso(usuarioActualProvider.actual(), ticket.getAreaActual().getId());
        return ticket;
    }
}

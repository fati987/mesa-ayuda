package com.mesaayuda.llamada;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.auth.UsuarioActualProvider;
import com.mesaayuda.contacto.Contacto;
import com.mesaayuda.contacto.ContactoService;
import com.mesaayuda.llamada.dto.LlamadaCrearRequest;
import com.mesaayuda.llamada.dto.LlamadaDetalleDto;
import com.mesaayuda.llamada.mapper.LlamadaMapper;
import com.mesaayuda.usuario.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class LlamadaService {

    private final LlamadaRepository llamadaRepository;
    private final ContactoService contactoService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActualProvider usuarioActualProvider;

    public LlamadaService(LlamadaRepository llamadaRepository, ContactoService contactoService,
            UsuarioRepository usuarioRepository, UsuarioActualProvider usuarioActualProvider) {
        this.llamadaRepository = llamadaRepository;
        this.contactoService = contactoService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActualProvider = usuarioActualProvider;
    }

    @Transactional
    public LlamadaDetalleDto crear(LlamadaCrearRequest request) {
        Contacto contacto = contactoService.obtenerOCrear(
                request.contactoTelefono(), request.contactoNombreCompleto(), request.contactoCorreo());

        Llamada llamada = new Llamada();
        llamada.setContacto(contacto);
        llamada.setUsuarioAtiende(usuarioRepository.getReferenceById(usuarioActualProvider.actual().getId()));
        llamada.setFechaHora(Instant.now());
        llamada.setDuracionSegundos(request.duracionSegundos());

        return LlamadaMapper.aDetalle(llamadaRepository.save(llamada));
    }
}

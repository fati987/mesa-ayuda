package com.mesaayuda.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaNoEncontradaException;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.auth.UsuarioActualProvider;
import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.usuario.dto.UsuarioActualizarRequest;
import com.mesaayuda.usuario.dto.UsuarioCambiarContrasenaRequest;
import com.mesaayuda.usuario.dto.UsuarioCrearRequest;
import com.mesaayuda.usuario.dto.UsuarioDetalleDto;
import com.mesaayuda.usuario.dto.UsuarioResumenDto;
import com.mesaayuda.usuario.mapper.UsuarioMapper;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AreaRepository areaRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioActualProvider usuarioActualProvider;

    public UsuarioService(UsuarioRepository usuarioRepository, AreaRepository areaRepository,
            PasswordEncoder passwordEncoder, UsuarioActualProvider usuarioActualProvider) {
        this.usuarioRepository = usuarioRepository;
        this.areaRepository = areaRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioActualProvider = usuarioActualProvider;
    }

    public PaginaResponse<UsuarioResumenDto> listar(Pageable pageable) {
        Page<Usuario> pagina = usuarioRepository.findAll(pageable);
        return PaginaResponse.de(pagina, UsuarioMapper::aResumen);
    }

    public UsuarioDetalleDto obtenerPorCorreo(String correo) {
        return UsuarioMapper.aDetalle(buscarPorCorreo(correo));
    }

    @Transactional
    public UsuarioDetalleDto crear(UsuarioCrearRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new CorreoYaRegistradoException(request.correo());
        }
        Area area = areaRepository.findById(request.areaId())
                .orElseThrow(() -> new AreaNoEncontradaException(request.areaId()));

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setCorreo(request.correo());
        usuario.setRol(request.rol());
        usuario.setArea(area);
        usuario.setContrasenaHash(passwordEncoder.encode(request.contrasena()));
        usuario.setActivo(true);

        return UsuarioMapper.aDetalle(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDetalleDto actualizar(String correo, UsuarioActualizarRequest request) {
        Usuario usuario = buscarPorCorreo(correo);
        if (!request.activo() && correo.equals(usuarioActualProvider.actual().getUsername())) {
            throw new AutodesactivacionNoPermitidaException();
        }
        Area area = areaRepository.findById(request.areaId())
                .orElseThrow(() -> new AreaNoEncontradaException(request.areaId()));

        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setRol(request.rol());
        usuario.setArea(area);
        usuario.setActivo(request.activo());

        return UsuarioMapper.aDetalle(usuario);
    }

    @Transactional
    public void desactivar(String correo) {
        if (correo.equals(usuarioActualProvider.actual().getUsername())) {
            throw new AutodesactivacionNoPermitidaException();
        }
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setActivo(false);
    }

    @Transactional
    public void cambiarContrasena(String correo, UsuarioCambiarContrasenaRequest request) {
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setContrasenaHash(passwordEncoder.encode(request.contrasenaNueva()));
    }

    private Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException(correo));
    }
}

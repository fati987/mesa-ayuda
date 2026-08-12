package com.mesaayuda.usuario;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.shared.paginacion.PaginaResponse;
import com.mesaayuda.usuario.dto.UsuarioActualizarRequest;
import com.mesaayuda.usuario.dto.UsuarioCambiarContrasenaRequest;
import com.mesaayuda.usuario.dto.UsuarioCrearRequest;
import com.mesaayuda.usuario.dto.UsuarioDetalleDto;
import com.mesaayuda.usuario.dto.UsuarioResumenDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public PaginaResponse<UsuarioResumenDto> listar(@PageableDefault(size = 20) Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @GetMapping("/{correo}")
    public UsuarioDetalleDto obtener(@PathVariable String correo) {
        return usuarioService.obtenerPorCorreo(correo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDetalleDto crear(@Valid @RequestBody UsuarioCrearRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{correo}")
    public UsuarioDetalleDto actualizar(@PathVariable String correo, @Valid @RequestBody UsuarioActualizarRequest request) {
        return usuarioService.actualizar(correo, request);
    }

    @DeleteMapping("/{correo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable String correo) {
        usuarioService.desactivar(correo);
    }

    @PutMapping("/{correo}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@PathVariable String correo, @Valid @RequestBody UsuarioCambiarContrasenaRequest request) {
        usuarioService.cambiarContrasena(correo, request);
    }
}

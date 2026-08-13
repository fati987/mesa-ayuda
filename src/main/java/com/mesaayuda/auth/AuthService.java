package com.mesaayuda.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.AreaNoEncontradaException;
import com.mesaayuda.area.AreaRepository;
import com.mesaayuda.auth.dto.LoginRequest;
import com.mesaayuda.auth.dto.LoginResponse;
import com.mesaayuda.auth.dto.RefreshRequest;
import com.mesaayuda.auth.dto.RefreshResponse;
import com.mesaayuda.auth.dto.UsuarioActualDto;
import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.UsuarioRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioActualProvider usuarioActualProvider;
    private final AreaRepository areaRepository;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            UsuarioActualProvider usuarioActualProvider, AreaRepository areaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioActualProvider = usuarioActualProvider;
        this.areaRepository = areaRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreoAndActivoTrue(request.correo())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!passwordEncoder.matches(request.contrasena(), usuario.getContrasenaHash())) {
            // Mismo mensaje que "no encontrado": no revelar cuál de los dos falló.
            throw new CredencialesInvalidasException();
        }
        return new LoginResponse(
                jwtService.generarAccessToken(usuario),
                jwtService.generarRefreshToken(usuario),
                "Bearer",
                jwtService.accessTokenExpiracionSegundos());
    }

    public RefreshResponse refrescar(RefreshRequest request) {
        Jws<Claims> claims;
        try {
            claims = jwtService.parsearYValidar(request.refreshToken());
        } catch (JwtException ex) {
            throw new TokenInvalidoException();
        }
        if (!jwtService.esRefreshToken(claims)) {
            throw new TokenInvalidoException();
        }
        Usuario usuario = usuarioRepository.findByCorreoAndActivoTrue(jwtService.extraerCorreo(claims))
                .orElseThrow(TokenInvalidoException::new);
        return new RefreshResponse(jwtService.generarAccessToken(usuario), "Bearer");
    }

    public UsuarioActualDto me() {
        UsuarioPrincipal actual = usuarioActualProvider.actual();
        Area area = areaRepository.findById(actual.getAreaId())
                .orElseThrow(() -> new AreaNoEncontradaException(actual.getAreaId()));
        return new UsuarioActualDto(actual.getNombreCompleto(), actual.getUsername(), actual.getRol(),
                actual.getAreaId(), area.getNombre());
    }
}

package com.mesaayuda.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.auth.dto.LoginRequest;
import com.mesaayuda.auth.dto.LoginResponse;
import com.mesaayuda.auth.dto.RefreshRequest;
import com.mesaayuda.auth.dto.RefreshResponse;
import com.mesaayuda.auth.dto.UsuarioActualDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refrescar(request);
    }

    @GetMapping("/me")
    public UsuarioActualDto me() {
        return authService.me();
    }
}

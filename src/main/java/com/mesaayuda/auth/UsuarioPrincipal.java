package com.mesaayuda.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mesaayuda.usuario.Usuario;
import com.mesaayuda.usuario.enums.Rol;

/**
 * Copia los campos primitivos del Usuario en vez de envolver la entidad JPA
 * viva: evita LazyInitializationException si algo lee areaId/rol fuera de
 * la sesión de Hibernate que lo originó (open-in-view: false).
 */
public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String correo;
    private final String nombreCompleto;
    private final Rol rol;
    private final Long areaId;
    private final boolean activo;

    public UsuarioPrincipal(Usuario usuario) {
        this(usuario.getId(), usuario.getCorreo(), usuario.getNombreCompleto(),
                usuario.getRol(), usuario.getArea().getId(), usuario.isActivo());
    }

    public UsuarioPrincipal(Long id, String correo, String nombreCompleto, Rol rol, Long areaId, boolean activo) {
        this.id = id;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.areaId = areaId;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    public Long getAreaId() {
        return areaId;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}

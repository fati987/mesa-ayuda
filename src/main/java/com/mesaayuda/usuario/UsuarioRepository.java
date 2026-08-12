package com.mesaayuda.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoAndActivoTrue(String correo);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}

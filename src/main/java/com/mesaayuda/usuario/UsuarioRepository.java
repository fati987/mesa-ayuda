package com.mesaayuda.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mesaayuda.usuario.enums.Rol;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoAndActivoTrue(String correo);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRolAndArea_IdAndActivoTrue(Rol rol, Long areaId);
}

package com.mesaayuda.contacto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    // telefono no es único (una persona puede llamar desde distintos
    // números, o distintos contactos compartir uno): se toma el primero.
    Optional<Contacto> findFirstByTelefonoOrderByIdAsc(String telefono);
}

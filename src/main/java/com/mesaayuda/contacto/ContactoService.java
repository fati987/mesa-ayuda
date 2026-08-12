package com.mesaayuda.contacto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContactoService {

    private final ContactoRepository contactoRepository;

    public ContactoService(ContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
    }

    @Transactional
    public Contacto obtenerOCrear(String telefono, String nombreCompleto, String correo) {
        return contactoRepository.findFirstByTelefonoOrderByIdAsc(telefono)
                .orElseGet(() -> crear(telefono, nombreCompleto, correo));
    }

    private Contacto crear(String telefono, String nombreCompleto, String correo) {
        Contacto contacto = new Contacto();
        contacto.setTelefono(telefono);
        contacto.setNombreCompleto(nombreCompleto);
        contacto.setCorreo(correo);
        contacto.setActivo(true);
        return contactoRepository.save(contacto);
    }
}

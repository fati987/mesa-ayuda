package com.mesaayuda.derivacion;

import org.springframework.stereotype.Service;

import com.mesaayuda.area.Area;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.usuario.Usuario;

/**
 * Solo inserta filas de Derivacion (regla invariable #9: solo-inserción).
 * Se invoca dentro de la transacción que ya abrió TransicionService, así
 * que no necesita su propio @Transactional.
 */
@Service
public class DerivacionService {

    private final DerivacionRepository derivacionRepository;

    public DerivacionService(DerivacionRepository derivacionRepository) {
        this.derivacionRepository = derivacionRepository;
    }

    public void registrar(Ticket ticket, Area areaOrigen, Area areaDestino, Usuario usuarioDeriva, String motivo) {
        Derivacion derivacion = new Derivacion();
        derivacion.setTicket(ticket);
        derivacion.setAreaOrigen(areaOrigen);
        derivacion.setAreaDestino(areaDestino);
        derivacion.setUsuarioDeriva(usuarioDeriva);
        derivacion.setMotivo(motivo);
        derivacionRepository.save(derivacion);
    }
}

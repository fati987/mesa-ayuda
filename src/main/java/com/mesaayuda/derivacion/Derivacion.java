package com.mesaayuda.derivacion;

import com.mesaayuda.area.Area;
import com.mesaayuda.shared.audit.AuditableCreacion;
import com.mesaayuda.ticket.Ticket;
import com.mesaayuda.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Solo-inserción: nunca se actualiza ni se borra (regla invariable #9).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "derivacion")
public class Derivacion extends AuditableCreacion {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_origen_id", nullable = false)
    private Area areaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_destino_id", nullable = false)
    private Area areaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_deriva_id", nullable = false)
    private Usuario usuarioDeriva;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;
}

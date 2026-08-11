package com.mesaayuda.llamada;

import java.time.Instant;

import com.mesaayuda.contacto.Contacto;
import com.mesaayuda.shared.audit.Auditable;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "llamada")
public class Llamada extends Auditable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_atiende_id", nullable = false)
    private Usuario usuarioAtiende;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;
}

package com.mesaayuda.ticket;

import java.time.Instant;

import com.mesaayuda.area.Area;
import com.mesaayuda.categoria.Categoria;
import com.mesaayuda.contacto.Contacto;
import com.mesaayuda.llamada.Llamada;
import com.mesaayuda.shared.audit.Auditable;
import com.mesaayuda.ticket.enums.EstadoTicket;
import com.mesaayuda.ticket.enums.Impacto;
import com.mesaayuda.ticket.enums.Origen;
import com.mesaayuda.ticket.enums.Prioridad;
import com.mesaayuda.ticket.enums.TipoTicket;
import com.mesaayuda.ticket.enums.Urgencia;
import com.mesaayuda.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "ticket")
public class Ticket extends Auditable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "llamada_id")
    private Llamada llamada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_actual_id", nullable = false)
    private Area areaActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creador_id", nullable = false)
    private Usuario usuarioCreador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTicket tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 25)
    private EstadoTicket estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 10)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia", nullable = false, length = 10)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "impacto", nullable = false, length = 15)
    private Impacto impacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 10)
    private Origen origen;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(name = "resuelto_en_llamada", nullable = false)
    private boolean resueltoEnLlamada;

    @Column(name = "solucion", columnDefinition = "text")
    private String solucion;

    @Column(name = "minutos_pausado", nullable = false)
    private int minutosPausado;

    @Column(name = "eliminado_en")
    private Instant eliminadoEn;
}

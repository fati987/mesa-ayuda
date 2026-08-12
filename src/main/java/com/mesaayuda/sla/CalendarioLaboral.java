package com.mesaayuda.sla;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

import com.mesaayuda.shared.audit.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "calendario_laboral")
public class CalendarioLaboral extends Auditable {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "zona_horaria", nullable = false, length = 50)
    private String zonaHoraria;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "aplica_lunes", nullable = false)
    private boolean aplicaLunes;

    @Column(name = "aplica_martes", nullable = false)
    private boolean aplicaMartes;

    @Column(name = "aplica_miercoles", nullable = false)
    private boolean aplicaMiercoles;

    @Column(name = "aplica_jueves", nullable = false)
    private boolean aplicaJueves;

    @Column(name = "aplica_viernes", nullable = false)
    private boolean aplicaViernes;

    @Column(name = "aplica_sabado", nullable = false)
    private boolean aplicaSabado;

    @Column(name = "aplica_domingo", nullable = false)
    private boolean aplicaDomingo;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public boolean aplicaA(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> aplicaLunes;
            case TUESDAY -> aplicaMartes;
            case WEDNESDAY -> aplicaMiercoles;
            case THURSDAY -> aplicaJueves;
            case FRIDAY -> aplicaViernes;
            case SATURDAY -> aplicaSabado;
            case SUNDAY -> aplicaDomingo;
        };
    }

    public ZoneId zoneId() {
        return ZoneId.of(zonaHoraria);
    }
}

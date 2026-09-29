package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "suscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Suscripcion {

    public enum Estado { ACTIVA, CANCELADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSuscripcion")
    private Integer idSuscripcion;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCliente", nullable = false)
    private Cliente cliente;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPlan", nullable = false)
    private Plan plan;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    /** Precio que se pactó al suscribirse: si luego cambia el precio del plan, el histórico no se altera. */
    @Column(name = "precio_pagado", nullable = false)
    private Integer precioPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private Estado estado;

    /** Para no enviar más de un correo de "tu plan está por vencer" por suscripción. */
    @Column(name = "aviso_vencimiento_enviado")
    // Boolean (no boolean): las suscripciones creadas antes de existir esta columna la tienen en NULL
    private Boolean avisoVencimientoEnviado;

    /** Nombre distinto del getter de Lombok a propósito: un isX() manual choca con él en el compilador de VS Code. */
    public boolean yaFueAvisada() {
        return Boolean.TRUE.equals(avisoVencimientoEnviado);
    }

    /** Días de anticipación con que se avisa al cliente de que su plan vence. */
    public static final int DIAS_AVISO = 5;

    public boolean isPorVencer() {
        return isVigente() && getDiasRestantes() <= DIAS_AVISO;
    }

    /** Activa y dentro de su periodo. Una suscripción ACTIVA cuya fecha de fin ya pasó está vencida. */
    public boolean isVigente() {
        return estado == Estado.ACTIVA && !fechaFin.isBefore(LocalDate.now());
    }

    public long getDiasRestantes() {
        return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), fechaFin));
    }

    public String getEstadoTexto() {
        if (estado == Estado.CANCELADA) return "Cancelada";
        return isVigente() ? "Activa" : "Vencida";
    }
}

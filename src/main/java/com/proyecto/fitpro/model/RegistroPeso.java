package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/** Una medición de peso del cliente. Se guarda como máximo una por día (la última del día manda). */
@Entity
@Table(name = "registro_peso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPeso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRegistro")
    private Integer idRegistro;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "peso", nullable = false)
    private Double peso;

    @Column(name = "altura", nullable = false)
    private Double altura;

    public double getImc() {
        return peso / (altura * altura);
    }
}

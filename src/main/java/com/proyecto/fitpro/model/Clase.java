package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "Clase")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClase")
    private Integer idClase;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "capacidad")
    private Integer capacidad;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "hora")
    private LocalTime hora;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Entrenador_idEntrenador")
    private Entrenador entrenador;

    @ManyToMany(mappedBy = "clases", fetch = FetchType.LAZY)
    private List<Cliente> clientes;
}

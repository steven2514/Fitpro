package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Ejercicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEjercicio")
    private Integer idEjercicio;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "series")
    private Integer series;

    @Column(name = "repeticiones")
    private Integer repeticiones;

    @Column(name = "descanso_segundos")
    private Integer descansoSegundos;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Rutina_idRutina")
    private Rutina rutina;
}

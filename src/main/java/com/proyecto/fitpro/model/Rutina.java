package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "rutina")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRutina")
    private Integer idRutina;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    /** Entrenador que diseñó la rutina; null si la creó un administrador. */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Entrenador_idEntrenador")
    private Entrenador entrenador;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "objetivo", length = 100)
    private String objetivo;

    @Column(name = "nivel", length = 50)
    private String nivel;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "rutina", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ejercicio> ejercicios;
}

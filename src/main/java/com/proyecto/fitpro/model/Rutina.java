package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "Rutina")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRutina")
    private Integer idRutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "objetivo", length = 100)
    private String objetivo;

    @Column(name = "nivel", length = 50)
    private String nivel;

    @OneToMany(mappedBy = "rutina", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ejercicio> ejercicios;
}

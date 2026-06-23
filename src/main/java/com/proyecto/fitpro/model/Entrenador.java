package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "Entrenador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEntrenador")
    private Integer idEntrenador;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "especialidad", length = 50)
    private String especialidad;

    @Column(name = "horario", length = 50)
    private String horario;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @OneToMany(mappedBy = "entrenador", fetch = FetchType.LAZY)
    private List<Clase> clases;
}

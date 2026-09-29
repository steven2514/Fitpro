package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "entrenador")
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

    /** Hash BCrypt. Si es null, el entrenador no tiene acceso a la aplicación. */
    @ToString.Exclude
    @Column(name = "password", length = 255)
    private String password;

    public boolean isTieneAcceso() {
        return password != null;
    }

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "entrenador", fetch = FetchType.LAZY)
    private List<Clase> clases;
}

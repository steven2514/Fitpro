package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Administrador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdministrador")
    private Integer idAdministrador;

    @Column(name = "apellido", length = 50)
    private String apellido;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "password", length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Cliente_idCliente")
    private Cliente cliente;
}

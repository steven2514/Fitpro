package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Alimentacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAlimentacion")
    private Integer idAlimentacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    @Column(name = "tipo_comida", length = 50)
    private String tipoComida;

    @Column(name = "calorias")
    private Integer calorias;

    @Column(name = "proteinas_gramos")
    private Double proteinasGramos;

    @Column(name = "carbohidratos_gramos")
    private Double carbohidratosGramos;

    @Column(name = "grasas_gramos")
    private Double grasasGramos;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}

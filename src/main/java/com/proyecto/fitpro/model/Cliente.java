package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCliente")
    private Integer idCliente;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "documento", unique = true, length = 30)
    private String documento;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "peso")
    private Double peso;

    @Column(name = "altura")
    private Double altura;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "genero", length = 20)
    private String genero;

    @Column(name = "objetivo_fitness", length = 100)
    private String objetivoFitness;

    @Column(name = "nivel_actividad", length = 50)
    private String nivelActividad;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Administrador> administradores;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Rutina> rutinas;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Alimentacion> alimentaciones;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "Cliente_has_Clase",
            joinColumns = @JoinColumn(name = "Cliente_idCliente"),
            inverseJoinColumns = @JoinColumn(name = "Clase_idClase")
    )
    private List<Clase> clases;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
    }

    public Double calcularIMC() {
        if (peso != null && altura != null && altura > 0) {
            return peso / (altura * altura);
        }
        return null;
    }

    public String obtenerCategoriaIMC() {
        Double imc = calcularIMC();
        if (imc == null) return "No calculado";
        if (imc < 18.5) return "Bajo peso";
        if (imc < 25) return "Peso normal";
        if (imc < 30) return "Sobrepeso";
        return "Obesidad";
    }

    public String getIMCFormateado() {
        Double imc = calcularIMC();
        if (imc == null) return "N/A";
        return String.format("%.2f", imc);
    }

    public Double getPesoIdeal() {
        if (altura != null && altura > 0) {
            return 22.5 * (altura * altura);
        }
        return null;
    }

    public String getPesoIdealFormateado() {
        Double pesoIdeal = getPesoIdeal();
        if (pesoIdeal == null) return "N/A";
        return String.format("%.1f kg", pesoIdeal);
    }

    public String getColorCategoriaIMC() {
        Double imc = calcularIMC();
        if (imc == null) return "#888";
        if (imc < 18.5) return "#f59e0b";
        if (imc < 25) return "#10b981";
        if (imc < 30) return "#f59e0b";
        return "#ef4444";
    }
}

package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCliente")
    private Integer idCliente;

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    @Column(name = "documento", unique = true, length = 30)
    private String documento;

    @Email(message = "Email inválido")
    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Teléfono inválido")
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 100)
    private String direccion;

    // Guarda el hash BCrypt; la longitud mínima se valida sobre la contraseña en claro, en los DTO
    @Column(name = "password", length = 255)
    private String password;

    @Positive(message = "El peso debe ser mayor a 0")
    @DecimalMin(value = "30.0", message = "El peso debe ser al menos 30 kg")
    @DecimalMax(value = "300.0", message = "El peso no puede exceder 300 kg")
    @Column(name = "peso")
    private Double peso;

    @Positive(message = "La altura debe ser mayor a 0")
    @DecimalMin(value = "1.40", message = "La altura debe ser al menos 1.40 m")
    @DecimalMax(value = "2.20", message = "La altura no puede exceder 2.20 m")
    @Column(name = "altura")
    private Double altura;

    @Min(value = 13, message = "La edad mínima es 13 años")
    @Max(value = 100, message = "La edad no puede exceder 100 años")
    @Column(name = "edad")
    private Integer edad;

    @Pattern(regexp = "^(Masculino|Femenino|Otro)$|^$", message = "Género inválido")
    @Column(name = "genero", length = 20)
    private String genero;

    @Column(name = "objetivo_fitness", length = 100)
    private String objetivoFitness;

    @Column(name = "nivel_actividad", length = 50)
    private String nivelActividad;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Rutina> rutinas;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Alimentacion> alimentaciones;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cliente_has_clase",
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

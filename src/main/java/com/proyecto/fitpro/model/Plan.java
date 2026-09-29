package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Plan de suscripción que un cliente puede contratar (p. ej. Básico, Pro, Élite).
 * La tabla no se llama "plan" para no chocar con palabras reservadas de algunos motores SQL.
 */
@Entity
@Table(name = "plan_suscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPlan")
    private Integer idPlan;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** Precio mensual en pesos colombianos, sin decimales. */
    @Column(name = "precio_mensual", nullable = false)
    private Integer precioMensual;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    /** Un beneficio por línea. */
    @Column(name = "beneficios", columnDefinition = "TEXT")
    private String beneficios;

    /** Si el plan permite al cliente inscribirse por su cuenta en clases grupales. */
    @Column(name = "incluye_clases", nullable = false)
    private boolean incluyeClases;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public List<String> getListaBeneficios() {
        if (beneficios == null || beneficios.isBlank()) return List.of();
        return Arrays.stream(beneficios.split("\\R")).map(String::trim).filter(b -> !b.isEmpty()).toList();
    }

    public String getPrecioFormateado() {
        return precioMensual == null ? "-" : String.format(COLOMBIA, "$%,d", precioMensual);
    }
}

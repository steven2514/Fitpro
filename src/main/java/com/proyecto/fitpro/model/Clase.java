package com.proyecto.fitpro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "clase")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Igualdad por id: las clases van en el Set de inscripciones del cliente, y si se compararan
// por todos sus campos, editar una clase ya cargada haría que no se encontrara al quitarla.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Clase {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClase")
    private Integer idClase;

    @Column(name = "nombre", length = 50)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "capacidad")
    private Integer capacidad;

    /** Columna heredada del esquema original; las clases se programan por día de la semana. */
    @Column(name = "fecha")
    private LocalDate fecha;

    /** Día de la semana en que se repite la clase. */
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", length = 10)
    private DayOfWeek diaSemana;

    @Column(name = "hora")
    private LocalTime hora;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    private static final Locale ES = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public boolean isProgramada() {
        return diaSemana != null && hora != null;
    }

    public String getDiaTexto() {
        if (diaSemana == null) return "Sin día";
        String dia = diaSemana.getDisplayName(TextStyle.FULL, ES);
        return Character.toUpperCase(dia.charAt(0)) + dia.substring(1);
    }

    /** Ej.: "18:00 – 19:00". */
    public String getHoraTexto() {
        if (hora == null) return "Sin hora";
        String inicio = hora.format(HH_MM);
        return duracionMinutos == null ? inicio : inicio + " – " + hora.plusMinutes(duracionMinutos).format(HH_MM);
    }

    /** Ej.: "Lunes 18:00 – 19:00", o "Horario por definir". */
    public String getHorarioTexto() {
        return isProgramada() ? getDiaTexto() + " " + getHoraTexto() : "Horario por definir";
    }

    /** Porcentaje de cupos ocupados (0-100); 0 si la clase no tiene capacidad definida. */
    public int getOcupacion() {
        if (capacidad == null || capacidad == 0) return 0;
        return Math.min(100, Math.round(getInscritos() * 100f / capacidad));
    }

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Entrenador_idEntrenador")
    private Entrenador entrenador;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "clases", fetch = FetchType.LAZY)
    private List<Cliente> clientes;

    public int getInscritos() {
        return clientes == null ? 0 : clientes.size();
    }

    /** Cupos libres; si la clase no tiene capacidad definida se considera sin límite. */
    public int getCuposDisponibles() {
        return capacidad == null ? Integer.MAX_VALUE : Math.max(0, capacidad - getInscritos());
    }

    public boolean isLlena() {
        return getCuposDisponibles() == 0;
    }
}

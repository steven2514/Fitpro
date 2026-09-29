package com.proyecto.fitpro.dto;

import com.proyecto.fitpro.model.Plan;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanDTO {

    @NotBlank(message = "El nombre del plan es requerido")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es requerido")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Max(value = 10_000_000, message = "El precio es demasiado alto")
    private Integer precioMensual;

    @NotNull(message = "La duración es requerida")
    @Min(value = 1, message = "La duración debe ser de al menos 1 día")
    @Max(value = 366, message = "La duración no puede superar un año")
    private Integer duracionDias;

    @Size(max = 2000, message = "Los beneficios son demasiado largos")
    private String beneficios;

    private boolean incluyeClases;
    private boolean destacado;

    public static PlanDTO fromEntity(Plan plan) {
        PlanDTO dto = new PlanDTO();
        dto.setNombre(plan.getNombre());
        dto.setDescripcion(plan.getDescripcion());
        dto.setPrecioMensual(plan.getPrecioMensual());
        dto.setDuracionDias(plan.getDuracionDias());
        dto.setBeneficios(plan.getBeneficios());
        dto.setIncluyeClases(plan.isIncluyeClases());
        dto.setDestacado(plan.isDestacado());
        return dto;
    }

    public void aplicarA(Plan plan) {
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion);
        plan.setPrecioMensual(precioMensual);
        plan.setDuracionDias(duracionDias);
        plan.setBeneficios(beneficios);
        plan.setIncluyeClases(incluyeClases);
        plan.setDestacado(destacado);
    }
}

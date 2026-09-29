package com.proyecto.fitpro.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EjercicioDTO {

    @NotBlank(message = "El nombre del ejercicio es requerido")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotNull(message = "Las series son requeridas")
    @Min(value = 1, message = "Debe haber al menos 1 serie")
    @Max(value = 20, message = "No puede haber más de 20 series")
    private Integer series;

    @NotNull(message = "Las repeticiones son requeridas")
    @Min(value = 1, message = "Debe haber al menos 1 repetición")
    @Max(value = 200, message = "No puede haber más de 200 repeticiones")
    private Integer repeticiones;

    @Min(value = 0, message = "El descanso no puede ser negativo")
    @Max(value = 600, message = "El descanso no puede superar 10 minutos")
    private Integer descansoSegundos;

    @Size(max = 500, message = "Las notas no pueden superar 500 caracteres")
    private String notas;
}

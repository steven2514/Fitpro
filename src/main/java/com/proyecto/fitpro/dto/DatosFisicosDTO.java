package com.proyecto.fitpro.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DatosFisicosDTO {

    @DecimalMin(value = "30.0", message = "El peso debe ser al menos 30 kg")
    @DecimalMax(value = "300.0", message = "El peso no puede exceder 300 kg")
    private Double peso;

    @DecimalMin(value = "1.40", message = "La altura debe ser al menos 1.40 m")
    @DecimalMax(value = "2.20", message = "La altura no puede exceder 2.20 m")
    private Double altura;

    @Min(value = 13, message = "La edad mínima es 13 años")
    @Max(value = 100, message = "La edad no puede exceder 100 años")
    private Integer edad;

    @Pattern(regexp = "^(Masculino|Femenino|Otro)$", message = "Género inválido")
    private String genero;

    @Size(max = 100, message = "El objetivo no puede superar 100 caracteres")
    private String objetivoFitness;

    @Size(max = 50, message = "El nivel de actividad no puede superar 50 caracteres")
    private String nivelActividad;
}

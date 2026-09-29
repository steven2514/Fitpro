package com.proyecto.fitpro.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nueva contraseña con confirmación. Se usa al cambiarla desde el perfil (con la actual) y al
 * restablecerla con un enlace de recuperación (sin la actual).
 */
@Data
@NoArgsConstructor
public class CambioPasswordDTO {

    private String actual;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String nueva;

    private String confirmacion;

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isConfirmada() {
        return nueva == null || nueva.equals(confirmacion);
    }
}

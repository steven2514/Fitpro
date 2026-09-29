package com.proyecto.fitpro.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdministradorDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    @Size(max = 50, message = "El apellido no puede superar 50 caracteres")
    private String apellido;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    @Size(max = 100, message = "El email no puede superar 100 caracteres")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;
}

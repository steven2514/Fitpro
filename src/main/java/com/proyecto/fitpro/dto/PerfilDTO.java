package com.proyecto.fitpro.dto;

import com.proyecto.fitpro.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos de contacto que el propio cliente puede cambiar. Nombre y documento sólo los cambia un admin. */
@Data
@NoArgsConstructor
public class PerfilDTO {

    @Email(message = "Email inválido")
    @Size(max = 100, message = "El email no puede superar 100 caracteres")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "La dirección no puede superar 100 caracteres")
    private String direccion;

    public static PerfilDTO fromEntity(Cliente cliente) {
        PerfilDTO dto = new PerfilDTO();
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        return dto;
    }
}

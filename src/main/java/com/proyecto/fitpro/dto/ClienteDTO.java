package com.proyecto.fitpro.dto;

import com.proyecto.fitpro.model.Cliente;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de cuenta de un cliente que llegan desde un formulario (registro, alta y edición
 * desde el panel de administración). No incluye el id ni las relaciones, para que un
 * formulario manipulado no pueda sobrescribir otra cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @NotBlank(message = "El documento es requerido")
    @Size(max = 30, message = "El documento no puede superar 30 caracteres")
    private String documento;

    @Email(message = "Email inválido")
    @Size(max = 100, message = "El email no puede superar 100 caracteres")
    private String email;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "La dirección no puede superar 100 caracteres")
    private String direccion;

    // Opcional al editar: vacío significa "no cambiar la contraseña"
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    public static ClienteDTO fromEntity(Cliente cliente) {
        return ClienteDTO.builder()
            .nombre(cliente.getNombre())
            .apellido(cliente.getApellido())
            .documento(cliente.getDocumento())
            .email(cliente.getEmail())
            .telefono(cliente.getTelefono())
            .direccion(cliente.getDireccion())
            .build();
    }

    /** Copia los datos de cuenta sobre la entidad; la contraseña se trata aparte porque hay que cifrarla. */
    public void aplicarA(Cliente cliente) {
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setDocumento(documento);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
    }
}

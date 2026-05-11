package com.comisiones.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO para recibir miembros desde JSON en el frontend
 */
public class MiembroDTO {

    @NotBlank(message = "El DNI/NIF es obligatorio")
    @Pattern(regexp = "^([0-9]{8}[A-Z]|[XYZ][0-9]{7}[A-Z])$",
             message = "Formato de DNI/NIF inválido (debe ser 12345678A o X1234567A)")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    private String nombre;

    private String rol;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    public MiembroDTO() {
        // Constructor por defecto para Jackson
        this.rol = "PARTICIPANTE"; // valor por defecto
        this.email = "";
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol != null ? rol : "PARTICIPANTE";
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isValid() {
        return dni != null && !dni.trim().isEmpty() 
            && nombre != null && !nombre.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "MiembroDTO{dni='" + dni + "', nombre='" + nombre + "', rol='" + rol + "'}";
    }
}

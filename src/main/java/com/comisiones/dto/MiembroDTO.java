package com.comisiones.dto;

/**
 * DTO para recibir miembros desde JSON en el frontend
 */
public class MiembroDTO {
    private String dni;
    private String nombre;
    private String rol;
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

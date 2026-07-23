package com.comisiones.dto;

/**
 * DTO para enviar comisiones como JSON al frontend
 */
public class ComisionDTO {
    private Long id;
    private String nombre;

    public ComisionDTO() {
        // Constructor por defecto para Jackson
    }

    public ComisionDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "ComisionDTO{id=" + id + ", nombre='" + nombre + "'}";
    }
}

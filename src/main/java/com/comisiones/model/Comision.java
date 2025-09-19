package com.comisiones.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Comision implements Serializable {
    
    private Long id;
    private String nombre;
    private Date fechaConstitucion;
    private Date fechaFin;
    private Date fechaCreacion;
    private Set<ComisionMiembro> miembros = new HashSet<>();
    
    public Comision() {}
    
    public Comision(String nombre, Date fechaConstitucion, Date fechaFin) {
        this.nombre = nombre;
        this.fechaConstitucion = fechaConstitucion;
        this.fechaFin = fechaFin;
    }
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Date getFechaConstitucion() { return fechaConstitucion; }
    public void setFechaConstitucion(Date fechaConstitucion) { this.fechaConstitucion = fechaConstitucion; }
    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Set<ComisionMiembro> getMiembros() { return miembros; }
    public void setMiembros(Set<ComisionMiembro> miembros) { this.miembros = miembros; }
}
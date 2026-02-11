package com.comisiones.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Comision implements Serializable {
 
    // Enums para Area y Tipo
    public enum Area {
        ATENCION_ESPECIALIZADA("Atención Especializada"),
        ATENCION_PRIMARIA("Atención Primaria"),
        MIXTA("Mixta");
        
        private String descripcion;
        
        Area(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public enum Tipo {
        COMISION("Comisión"),
        GRUPO_TRABAJO("Grupo de Trabajo"),
        GRUPO_MEJORA("Grupo de Mejora");
        
        private String descripcion;
        
        Tipo(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }    
    private Long id;
    private String nombre;
    private Area area;
    private Tipo tipo;
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
    
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    
    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; } 
    
    public Date getFechaConstitucion() { return fechaConstitucion; }
    public void setFechaConstitucion(Date fechaConstitucion) { this.fechaConstitucion = fechaConstitucion; }
    
    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }
    
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Set<ComisionMiembro> getMiembros() { return miembros; }
    public void setMiembros(Set<ComisionMiembro> miembros) { this.miembros = miembros; }
}
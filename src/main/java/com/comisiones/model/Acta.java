package com.comisiones.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util. Set;

public class Acta implements Serializable {
    
    private Long id;
    private Comision comision;
    private Date fechaReunion;
    private String observaciones;
    private Date fechaCreacion;
    private Set<AsistenciaActa> asistencias = new HashSet<>();
    
    public Acta() {}
    
    public Acta(Comision comision, Date fechaReunion, String observaciones) {
        this.comision = comision;
        this.fechaReunion = fechaReunion;
        this.observaciones = observaciones;
    }
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Comision getComision() {
        return comision;
    }
    
    public void setComision(Comision comision) {
        this.comision = comision;
    }
    
    public Date getFechaReunion() {
        return fechaReunion;
    }
    
    public void setFechaReunion(Date fechaReunion) {
        this.fechaReunion = fechaReunion;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Set<AsistenciaActa> getAsistencias() {
        return asistencias;
    }
    
    public void setAsistencias(Set<AsistenciaActa> asistencias) {
        this.asistencias = asistencias;
    }
    
    @Override
    public String toString() {
        return "Acta{" +
                "id=" + id +
                ", comision=" + (comision != null ? comision. getNombre() : "null") +
                ", fechaReunion=" + fechaReunion +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}


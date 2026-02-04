package com.comisiones.model;

import java.util.Date;

public class AsistenciaActa {
    private Long id;
    private Acta acta;
    private Miembro miembro;
    private boolean asistio;
    private String justificacion;  // ⭐ NUEVO CAMPO
    private Date fechaCreacion;
    
    public AsistenciaActa() {
        this.fechaCreacion = new Date();
    }
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio) {
        this.acta = acta;
        this.miembro = miembro;
        this.asistio = asistio;
        this.fechaCreacion = new Date();
    }
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio, String justificacion) {
        this.acta = acta;
        this.miembro = miembro;
        this.asistio = asistio;
        this.justificacion = justificacion;
        this.fechaCreacion = new Date();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Acta getActa() {
        return acta;
    }
    
    public void setActa(Acta acta) {
        this.acta = acta;
    }
    
    public Miembro getMiembro() {
        return miembro;
    }
    
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }
    
    public boolean isAsistio() {
        return asistio;
    }
    
    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
    }
    
    public String getJustificacion() {
        return justificacion;
    }
    
    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }
    
    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

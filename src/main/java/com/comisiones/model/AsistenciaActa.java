package com.comisiones.model;

import java.io.Serializable;

public class AsistenciaActa implements Serializable {
    
    private Long id;
    private Acta acta;
    private Miembro miembro;
    private boolean asistio;
    
    public AsistenciaActa() {}
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio) {
        this.acta = acta;
        this.miembro = miembro;
        this.asistio = asistio;
    }
    
    // Getters y setters
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
    
    @Override
    public String toString() {
        return "AsistenciaActa{" +
                "id=" + id +
                ", miembro=" + (miembro != null ? miembro. getNombreApellidos() : "null") +
                ", asistio=" + asistio +
                '}';
    }
}

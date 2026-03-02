package com.comisiones.model;

import java.time.LocalDateTime;

public class AsistenciaActa {
    private static final java.time.format.DateTimeFormatter FECHA_FORMATTER =
        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private Long id;
    private Acta acta;
    private Miembro miembro;
    private boolean asistio;
    private String justificacion;  // ⭐ NUEVO CAMPO
    private LocalDateTime fechaCreacion;
    
    public AsistenciaActa() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio) {
        this.acta = acta;
        this.miembro = miembro;
        this.asistio = asistio;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio, String justificacion) {
        this.acta = acta;
        this.miembro = miembro;
        this.asistio = asistio;
        this.justificacion = justificacion;
        this.fechaCreacion = LocalDateTime.now();
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
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    /**
     * Devuelve la fecha de creación formateada como String para uso en vistas JSP.
     * Alternativa a <fmt:formatDate> que no es compatible con LocalDateTime.
     */
    public String getFechaCreacionFormateada() {
        if (fechaCreacion == null) return "";
        return fechaCreacion.format(FECHA_FORMATTER);
    }
}
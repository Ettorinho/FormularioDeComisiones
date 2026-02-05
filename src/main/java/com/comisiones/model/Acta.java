package com.comisiones.model;

import java.util.Date;

public class Acta {
    private Long id;
    private Comision comision;
    private Date fechaReunion;
    private String observaciones;
    private Date fechaCreacion;
    
    // Campos para PDF adjunto
    private String pdfNombre;
    private byte[] pdfContenido;
    private String pdfTipoMime;
    
    // Constructores
    public Acta() {
    }
    
    // Getters y Setters
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
    
    // Getters y Setters para PDF
    public String getPdfNombre() {
        return pdfNombre;
    }
    
    public void setPdfNombre(String pdfNombre) {
        this.pdfNombre = pdfNombre;
    }
    
    public byte[] getPdfContenido() {
        return pdfContenido;
    }
    
    public void setPdfContenido(byte[] pdfContenido) {
        this.pdfContenido = pdfContenido;
    }
    
    public String getPdfTipoMime() {
        return pdfTipoMime;
    }
    
    public void setPdfTipoMime(String pdfTipoMime) {
        this.pdfTipoMime = pdfTipoMime;
    }
    
    // Método auxiliar para verificar si tiene PDF adjunto
    public boolean tienePdf() {
        return pdfNombre != null && !pdfNombre.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Acta{" +
                "id=" + id +
                ", comision=" + (comision != null ? comision.getNombre() : "null") +
                ", fechaReunion=" + fechaReunion +
                ", observaciones='" + observaciones + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", pdfNombre='" + pdfNombre + '\'' +
                ", tienePdf=" + tienePdf() +
                '}';
    }
}
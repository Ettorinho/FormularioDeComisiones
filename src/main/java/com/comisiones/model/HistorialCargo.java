package com.comisiones.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Modelo para el historial de cambios de cargo de miembros en comisiones.
 * Representa un registro de la tabla comision_miembro_historial_cargos.
 */
public class HistorialCargo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long comisionId;
    private Long miembroId;
    private String cargoAnterior;
    private String cargoNuevo;
    private Timestamp fechaCambio;
    private String motivo;
    private String usuarioModificacion;
    private Timestamp createdAt;
    private String createdBy;
    
    // Campos opcionales para JOINs
    private String nombreMiembro;
    private String nombreComision;
    
    public HistorialCargo() {
    }
    
    public HistorialCargo(Long comisionId, Long miembroId, String cargoAnterior, 
                         String cargoNuevo, Timestamp fechaCambio) {
        this.comisionId = comisionId;
        this.miembroId = miembroId;
        this.cargoAnterior = cargoAnterior;
        this.cargoNuevo = cargoNuevo;
        this.fechaCambio = fechaCambio;
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getComisionId() {
        return comisionId;
    }
    
    public void setComisionId(Long comisionId) {
        this.comisionId = comisionId;
    }
    
    public Long getMiembroId() {
        return miembroId;
    }
    
    public void setMiembroId(Long miembroId) {
        this.miembroId = miembroId;
    }
    
    public String getCargoAnterior() {
        return cargoAnterior;
    }
    
    public void setCargoAnterior(String cargoAnterior) {
        this.cargoAnterior = cargoAnterior;
    }
    
    public String getCargoNuevo() {
        return cargoNuevo;
    }
    
    public void setCargoNuevo(String cargoNuevo) {
        this.cargoNuevo = cargoNuevo;
    }
    
    public Timestamp getFechaCambio() {
        return fechaCambio;
    }
    
    public void setFechaCambio(Timestamp fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }
    
    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getNombreMiembro() {
        return nombreMiembro;
    }
    
    public void setNombreMiembro(String nombreMiembro) {
        this.nombreMiembro = nombreMiembro;
    }
    
    public String getNombreComision() {
        return nombreComision;
    }
    
    public void setNombreComision(String nombreComision) {
        this.nombreComision = nombreComision;
    }
    
    @Override
    public String toString() {
        return "HistorialCargo{" +
                "id=" + id +
                ", comisionId=" + comisionId +
                ", miembroId=" + miembroId +
                ", cargoAnterior='" + cargoAnterior + '\'' +
                ", cargoNuevo='" + cargoNuevo + '\'' +
                ", fechaCambio=" + fechaCambio +
                ", motivo='" + motivo + '\'' +
                ", usuarioModificacion='" + usuarioModificacion + '\'' +
                '}';
    }
}

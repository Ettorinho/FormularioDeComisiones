package com.comisiones.model;

import java.time.LocalDateTime;

public class AsistenciaActa {

    /** Estado detallado de la asistencia de un miembro a una reunión. */
    public static final String ESTADO_ASISTIO = "ASISTIO";
    public static final String ESTADO_EXCUSA = "EXCUSA";
    public static final String ESTADO_NO_ASISTIO = "NO_ASISTIO";

    private Long id;
    private Acta acta;
    private Miembro miembro;
    private boolean asistio;
    private String estadoAsistencia; // ASISTIO | EXCUSA | NO_ASISTIO
    private String justificacion;
    private String cargoMiembro;
    private LocalDateTime fechaCreacion;
    
    public AsistenciaActa() {
        this.fechaCreacion = LocalDateTime.now();
        this.estadoAsistencia = ESTADO_NO_ASISTIO;
    }
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio) {
        this.acta = acta;
        this.miembro = miembro;
        this.fechaCreacion = LocalDateTime.now();
        setAsistio(asistio);
    }
    
    public AsistenciaActa(Acta acta, Miembro miembro, boolean asistio, String justificacion) {
        this.acta = acta;
        this.miembro = miembro;
        this.justificacion = justificacion;
        this.fechaCreacion = LocalDateTime.now();
        setAsistio(asistio);
    }

    public AsistenciaActa(Acta acta, Miembro miembro, String estadoAsistencia, String justificacion) {
        this.acta = acta;
        this.miembro = miembro;
        this.justificacion = justificacion;
        this.fechaCreacion = LocalDateTime.now();
        setEstadoAsistencia(estadoAsistencia);
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
    
    /**
     * Establece la asistencia mediante el booleano heredado.
     * Se mantiene por compatibilidad; internamente sincroniza el nuevo
     * campo estadoAsistencia (ASISTIO cuando true, NO_ASISTIO cuando false,
     * salvo que ya hubiera un estado EXCUSA explícito, que se preserva).
     */
    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
        if (asistio) {
            this.estadoAsistencia = ESTADO_ASISTIO;
        } else if (!ESTADO_EXCUSA.equals(this.estadoAsistencia)) {
            this.estadoAsistencia = ESTADO_NO_ASISTIO;
        }
    }

    public String getEstadoAsistencia() {
        return estadoAsistencia;
    }

    /**
     * Establece el estado detallado de asistencia (ASISTIO | EXCUSA | NO_ASISTIO)
     * y sincroniza el booleano `asistio` (true únicamente para ASISTIO), de
     * forma que el código y las consultas existentes que dependen de
     * isAsistio() sigan funcionando sin cambios.
     */
    public void setEstadoAsistencia(String estadoAsistencia) {
        this.estadoAsistencia = (estadoAsistencia == null || estadoAsistencia.trim().isEmpty())
                ? ESTADO_NO_ASISTIO
                : estadoAsistencia.trim().toUpperCase();
        this.asistio = ESTADO_ASISTIO.equals(this.estadoAsistencia);
    }

    /** true si el miembro no asistió pero justificó su ausencia (confirmada en el formulario). */
    public boolean isExcusa() {
        return ESTADO_EXCUSA.equals(estadoAsistencia);
    }

    /** true si el miembro no asistió y no hay justificación registrada. */
    public boolean isNoAsistio() {
        return ESTADO_NO_ASISTIO.equals(estadoAsistencia);
    }
    
    public String getJustificacion() {
        return justificacion;
    }
    
    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getCargoMiembro() {
        return cargoMiembro;
    }

    public void setCargoMiembro(String cargoMiembro) {
        this.cargoMiembro = cargoMiembro;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

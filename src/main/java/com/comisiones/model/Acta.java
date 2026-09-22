package com.comisiones.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.comisiones.util.DateFormatUtil;

public class Acta {
    private Long id;

    @NotBlank(message = "El título del acta es obligatorio")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    private String titulo;

    @NotNull(message = "La comisión es obligatoria")
    private Comision comision;

    @NotNull(message = "La fecha de reunión es obligatoria")
    @PastOrPresent(message = "La fecha de reunión no puede ser futura")
    private LocalDate fechaReunion;

    @Size(max = 20000, message = "El resumen de la reunión no puede superar 20000 caracteres")
    private String observaciones;
    
    @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de inicio debe tener formato HH:mm")
    private String horaInicio;
    
    @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de fin debe tener formato HH:mm")
    private String horaFin;
    
    @Size(max = 100, message = "La duración no puede superar 100 caracteres")
    private String duracion;
    
    @Size(max = 20, message = "El tipo de reunión no puede superar 20 caracteres")
    private String tipoReunion;
    
    @Size(max = 1000, message = "El detalle de 'Otros' no puede superar 1000 caracteres")
    private String tipoReunionOtrosDetalle;
    
    @Size(max = 10000, message = "El orden del día no puede superar 10000 caracteres")
    private String ordenDia;
    
    @Size(max = 4000, message = "La excusa de asistencia no puede superar 4000 caracteres")
    private String excusaAsistencia;

    private LocalDateTime fechaCreacion;
    
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
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public Comision getComision() {
        return comision;
    }
    
    public void setComision(Comision comision) {
        this.comision = comision;
    }
    
    public LocalDate getFechaReunion() {
        return fechaReunion;
    }
    
    public void setFechaReunion(LocalDate fechaReunion) {
        this.fechaReunion = fechaReunion;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getTipoReunion() {
        return tipoReunion;
    }

    public void setTipoReunion(String tipoReunion) {
        this.tipoReunion = tipoReunion;
    }

    public String getTipoReunionOtrosDetalle() {
        return tipoReunionOtrosDetalle;
    }

    public void setTipoReunionOtrosDetalle(String tipoReunionOtrosDetalle) {
        this.tipoReunionOtrosDetalle = tipoReunionOtrosDetalle;
    }

    public String getOrdenDia() {
        return ordenDia;
    }

    public void setOrdenDia(String ordenDia) {
        this.ordenDia = ordenDia;
    }

    public String getExcusaAsistencia() {
        return excusaAsistencia;
    }

    public void setExcusaAsistencia(String excusaAsistencia) {
        this.excusaAsistencia = excusaAsistencia;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
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

    /**
     * Devuelve fechaReunion formateada como "dd/MM/yyyy".
     * Retorna cadena vacía si el valor es null (evita NPE en JSP).
     */
    public String getFechaReunionFormateada() {
        return DateFormatUtil.formatDate(fechaReunion);
    }

    /**
     * Devuelve fechaCreacion formateada como "dd/MM/yyyy 'a las' HH:mm".
     * Retorna cadena vacía si el valor es null (evita NPE en JSP).
     */
    public String getFechaCreacionFormateada() {
        return DateFormatUtil.formatDateTime(fechaCreacion);
    }

    @Override
    public String toString() {
        return "Acta{" +
                "id=" + id +
                ", comision=" + (comision != null ? comision.getNombre() : "null") +
                ", fechaReunion=" + fechaReunion +
                ", observaciones='" + observaciones + '\'' +
                ", horaInicio='" + horaInicio + '\'' +
                ", horaFin='" + horaFin + '\'' +
                ", duracion='" + duracion + '\'' +
                ", tipoReunion='" + tipoReunion + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", pdfNombre='" + pdfNombre + '\'' +
                ", tienePdf=" + tienePdf() +
                '}';
    }
}
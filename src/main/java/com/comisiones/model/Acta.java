package com.comisiones.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
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

    @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
    private String observaciones;
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
                ", fechaCreacion=" + fechaCreacion +
                ", pdfNombre='" + pdfNombre + '\'' +
                ", tienePdf=" + tienePdf() +
                '}';
    }
}
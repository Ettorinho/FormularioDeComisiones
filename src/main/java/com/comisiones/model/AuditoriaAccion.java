package com.comisiones.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuditoriaAccion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private LocalDateTime fechaHora;
    private String usuario;
    private String accion;
    private String entidad;
    private String entidadId;
    private String descripcion;
    private String ipOrigen;
    private String userAgent;
    private String sessionId;
    private String resultado;
    private String metodoHttp;
    private String urlSolicitada;
    private Integer duracionMs;
    private String mensajeError;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public String getEntidadId() { return entidadId; }
    public void setEntidadId(String entidadId) { this.entidadId = entidadId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getMetodoHttp() { return metodoHttp; }
    public void setMetodoHttp(String metodoHttp) { this.metodoHttp = metodoHttp; }

    public String getUrlSolicitada() { return urlSolicitada; }
    public void setUrlSolicitada(String urlSolicitada) { this.urlSolicitada = urlSolicitada; }

    public Integer getDuracionMs() { return duracionMs; }
    public void setDuracionMs(Integer duracionMs) { this.duracionMs = duracionMs; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
}

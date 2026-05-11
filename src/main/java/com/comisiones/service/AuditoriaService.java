package com.comisiones.service;

import com.comisiones.util.AppLogger;
import com.comisiones.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Servicio de auditoría centralizada.
 * Registra todas las acciones relevantes de los usuarios en la tabla auditoria_acciones.
 *
 * IMPORTANTE: Los métodos de este servicio NO lanzan excepciones al caller.
 * Si falla el registro de auditoría, se loguea el error pero la operación
 * de negocio continúa normalmente.
 *
 * Uso desde cualquier servlet:
 * <pre>
 *   AuditoriaService.getInstance().registrar(request, usuario, "CREAR", "COMISION",
 *       comision.getId().toString(), "Creó la comisión: " + comision.getNombre());
 * </pre>
 */
public class AuditoriaService {

    private static final AuditoriaService INSTANCE = new AuditoriaService();

    private static final String INSERT_SQL =
        "INSERT INTO auditoria_acciones (" +
        "fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen, " +
        "user_agent, resultado, duracion_ms, mensaje_error, sesion_id" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private AuditoriaService() {}

    public static AuditoriaService getInstance() {
        return INSTANCE;
    }

    /**
     * Registra una acción de auditoría.
     *
     * @param ipOrigen    IP del cliente (puede obtenerse con request.getRemoteAddr())
     * @param usuario     Username AD del usuario logueado (o "SISTEMA" si no hay sesión)
     * @param accion      Tipo: CREAR, MODIFICAR, ELIMINAR, BAJA, LOGIN, LOGOUT
     * @param entidad     Entidad afectada: COMISION, MIEMBRO, ACTA, CARGO, SESION
     * @param entidadId   ID del registro afectado (String, puede ser compuesto "comisionId/miembroId")
     * @param descripcion Descripción legible del cambio
     */
    public void registrar(String ipOrigen, String usuario, String accion,
                          String entidad, String entidadId, String descripcion) {
        registrar(ipOrigen, null, null, usuario, accion, entidad, entidadId, descripcion,
                "EXITOSO", null, null);
    }

    public void registrarExito(HttpServletRequest request, String usuario, String accion,
                               String entidad, String entidadId, String descripcion,
                               long inicioNanos) {
        registrar(extraerIpCliente(request), extraerUserAgent(request), extraerSesionId(request),
                usuario, accion, entidad, entidadId, descripcion, "EXITOSO",
                calcularDuracionMs(inicioNanos), null);
    }

    public void registrarFallo(HttpServletRequest request, String usuario, String accion,
                               String entidad, String entidadId, String descripcion,
                               long inicioNanos, String mensajeError) {
        registrar(extraerIpCliente(request), extraerUserAgent(request), extraerSesionId(request),
                usuario, accion, entidad, entidadId, descripcion, "FALLIDO",
                calcularDuracionMs(inicioNanos), truncarMensaje(mensajeError));
    }

    public void registrarDenegado(HttpServletRequest request, String usuario, String accion,
                                  String entidad, String entidadId, String descripcion) {
        registrar(extraerIpCliente(request), extraerUserAgent(request), extraerSesionId(request),
                usuario, accion, entidad, entidadId, descripcion, "DENEGADO",
                null, null);
    }

    private void registrar(String ipOrigen, String userAgent, String sesionId, String usuario, String accion,
                           String entidad, String entidadId, String descripcion, String resultado,
                           Integer duracionMs, String mensajeError) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, usuario != null ? usuario : "SISTEMA");
            stmt.setString(3, accion);
            stmt.setString(4, entidad);
            stmt.setString(5, entidadId);
            stmt.setString(6, descripcion);
            stmt.setString(7, ipOrigen);
            stmt.setString(8, userAgent);
            stmt.setString(9, resultado);
            stmt.setObject(10, duracionMs);
            stmt.setString(11, mensajeError);
            stmt.setString(12, sesionId);

            stmt.executeUpdate();
            AppLogger.debug("Auditoría registrada: [" + usuario + "] " + accion + " " + entidad
                    + (entidadId != null ? " #" + entidadId : "") + " - " + descripcion);

        } catch (SQLException e) {
            // NUNCA propagar el error: la auditoría no debe interrumpir el flujo de negocio
            AppLogger.error("Error al registrar auditoría [usuario=" + usuario
                    + ", accion=" + accion + ", entidad=" + entidad + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Sobrecarga conveniente que acepta directamente HttpServletRequest para extraer la IP.
     */
    public void registrar(HttpServletRequest request, String usuario,
                          String accion, String entidad, String entidadId, String descripcion) {
        registrar(extraerIpCliente(request), extraerUserAgent(request), extraerSesionId(request),
                usuario, accion, entidad, entidadId, descripcion, "EXITOSO", null, null);
    }

    private Integer calcularDuracionMs(long inicioNanos) {
        if (inicioNanos <= 0L) {
            return null;
        }
        long millis = (System.nanoTime() - inicioNanos) / 1_000_000L;
        return (int) Math.max(0L, millis);
    }

    private String extraerIpCliente(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            int commaIndex = forwardedFor.indexOf(',');
            String ip = commaIndex >= 0 ? forwardedFor.substring(0, commaIndex) : forwardedFor;
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private String extraerUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private String extraerSesionId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }

    private String truncarMensaje(String mensaje) {
        if (mensaje == null) {
            return null;
        }
        return mensaje.length() > 4000 ? mensaje.substring(0, 4000) : mensaje;
    }
}

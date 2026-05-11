package com.comisiones.service;

import com.comisiones.util.AppLogger;
import com.comisiones.util.DBUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Servicio de auditoría centralizada.
 * Registra todas las acciones relevantes de los usuarios en la tabla auditoria_acciones,
 * incluyendo metadatos de seguridad: IP real (X-Forwarded-For), user-agent, resultado,
 * duración de la operación, mensajes de error e ID de sesión.
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

    /** Resultado de auditoría: operación completada sin errores. */
    public static final String RESULTADO_EXITOSO  = "EXITOSO";
    /** Resultado de auditoría: operación que terminó con un error o excepción. */
    public static final String RESULTADO_FALLIDO  = "FALLIDO";
    /** Resultado de auditoría: operación denegada por falta de permisos. */
    public static final String RESULTADO_DENEGADO = "DENEGADO";

    private static final AuditoriaService INSTANCE = new AuditoriaService();

    private static final String INSERT_SQL =
        "INSERT INTO auditoria_acciones " +
        "(fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen, " +
        " user_agent, resultado, duracion_ms, mensaje_error, sesion_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private AuditoriaService() {}

    public static AuditoriaService getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Helper methods for request metadata extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the real client IP address with X-Forwarded-For proxy awareness.
     * Returns the first IP in the forwarded chain (original client), falling back
     * to {@code request.getRemoteAddr()}.
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Extracts and truncates the User-Agent header to at most 500 characters.
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            return null;
        }
        return ua.length() > 500 ? ua.substring(0, 500) : ua;
    }

    // -------------------------------------------------------------------------
    // Core registration methods
    // -------------------------------------------------------------------------

    /**
     * Registra una acción de auditoría con todos los metadatos disponibles.
     *
     * @param ipOrigen     IP del cliente
     * @param userAgent    User-Agent del cliente (puede ser null)
     * @param sesionId     ID de sesión HTTP (puede ser null)
     * @param usuario      Username AD del usuario logueado (o "SISTEMA" si no hay sesión)
     * @param accion       Tipo: CREAR, MODIFICAR, ELIMINAR, BAJA, LOGIN, LOGOUT
     * @param entidad      Entidad afectada: COMISION, MIEMBRO, ACTA, CARGO, SESION
     * @param entidadId    ID del registro afectado (puede ser null)
     * @param descripcion  Descripción legible del cambio (puede ser null)
     * @param resultado    EXITOSO, FALLIDO o DENEGADO
     * @param duracionMs   Tiempo de ejecución en milisegundos (puede ser null)
     * @param mensajeError Mensaje de error detallado (puede ser null)
     */
    public void registrar(String ipOrigen, String userAgent, String sesionId,
                          String usuario, String accion,
                          String entidad, String entidadId, String descripcion,
                          String resultado, Integer duracionMs, String mensajeError) {
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
            stmt.setString(9, resultado != null ? resultado : RESULTADO_EXITOSO);
            stmt.setObject(10, duracionMs);
            stmt.setString(11, mensajeError);
            stmt.setString(12, sesionId);

            stmt.executeUpdate();
            AppLogger.debug("Auditoría registrada: [" + usuario + "] " + accion + " " + entidad
                    + (entidadId != null ? " #" + entidadId : "")
                    + " resultado=" + resultado);

        } catch (SQLException e) {
            // NUNCA propagar el error: la auditoría no debe interrumpir el flujo de negocio
            AppLogger.error("Error al registrar auditoría [usuario=" + usuario
                    + ", accion=" + accion + ", entidad=" + entidad + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Registra una acción exitosa con IP directa (sin request).
     *
     * @param ipOrigen    IP del cliente
     * @param usuario     Username AD del usuario logueado (o "SISTEMA")
     * @param accion      Tipo de acción
     * @param entidad     Entidad afectada
     * @param entidadId   ID del registro afectado
     * @param descripcion Descripción legible del cambio
     */
    public void registrar(String ipOrigen, String usuario, String accion,
                          String entidad, String entidadId, String descripcion) {
        registrar(ipOrigen, null, null, usuario, accion, entidad, entidadId, descripcion,
                RESULTADO_EXITOSO, null, null);
    }

    /**
     * Registra una acción exitosa extrayendo automáticamente IP, user-agent y sesión
     * del {@link HttpServletRequest}.
     */
    public void registrar(HttpServletRequest request, String usuario,
                          String accion, String entidad, String entidadId, String descripcion) {
        String ip        = getClientIp(request);
        String ua        = getUserAgent(request);
        String sesion    = getSessionId(request);
        registrar(ip, ua, sesion, usuario, accion, entidad, entidadId, descripcion,
                RESULTADO_EXITOSO, null, null);
    }

    // -------------------------------------------------------------------------
    // High-level helpers
    // -------------------------------------------------------------------------

    /**
     * Wraps an operation with automatic audit logging, capturing execution duration
     * and any exceptions as FALLIDO results.  The original exception is always re-thrown
     * so the calling code can handle it normally.
     *
     * <pre>
     *   AuditoriaService.getInstance().auditarOperacion(
     *       request, usuario, "CREAR", "COMISION",
     *       () -> comisionDAO.insert(comision));
     * </pre>
     */
    public <T> T auditarOperacion(HttpServletRequest request, String usuario,
                                  String accion, String entidad, Supplier<T> operacion) {
        long inicio = System.currentTimeMillis();
        String ip     = getClientIp(request);
        String ua     = getUserAgent(request);
        String sesion = getSessionId(request);

        try {
            T resultado = operacion.get();
            int duracion = (int) (System.currentTimeMillis() - inicio);
            registrar(ip, ua, sesion, usuario, accion, entidad, null, null,
                    RESULTADO_EXITOSO, duracion, null);
            return resultado;

        } catch (RuntimeException e) {
            int duracion = (int) (System.currentTimeMillis() - inicio);
            registrar(ip, ua, sesion, usuario, accion, entidad, null, null,
                    RESULTADO_FALLIDO, duracion, e.getMessage());
            throw e;
        }
    }

    /**
     * Logs an access-denial event (e.g. authorization failure).
     *
     * @param request  HTTP request for metadata extraction
     * @param usuario  Username that was denied
     * @param recurso  Resource/action that was denied
     * @param motivo   Human-readable reason for the denial
     */
    public void registrarDenegacion(HttpServletRequest request, String usuario,
                                    String recurso, String motivo) {
        String ip     = getClientIp(request);
        String ua     = getUserAgent(request);
        String sesion = getSessionId(request);
        registrar(ip, ua, sesion, usuario, "ACCESS_DENIED", recurso, null, null,
                RESULTADO_DENEGADO, null, motivo);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static String getSessionId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }
}

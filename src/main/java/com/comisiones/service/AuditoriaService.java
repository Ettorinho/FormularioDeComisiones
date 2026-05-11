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
        "INSERT INTO auditoria_acciones " +
        "(fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen," +
        " user_agent, session_id, resultado, metodo_http, url_solicitada, duracion_ms, mensaje_error) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private AuditoriaService() {}

    public static AuditoriaService getInstance() {
        return INSTANCE;
    }

    /**
     * Registra una acción de auditoría con todos los metadatos disponibles.
     *
     * @param ipOrigen    IP del cliente (soporta cabeceras de proxy)
     * @param userAgent   Cabecera User-Agent del cliente HTTP (puede ser null)
     * @param sesionId    Identificador de sesión HTTP (puede ser null)
     * @param usuario     Username AD del usuario logueado (o "SISTEMA" si no hay sesión)
     * @param accion      Tipo: CREAR, MODIFICAR, ELIMINAR, BAJA, LOGIN, LOGOUT
     * @param entidad     Entidad afectada: COMISION, MIEMBRO, ACTA, CARGO, SESION
     * @param entidadId   ID del registro afectado (String, puede ser compuesto "comisionId/miembroId")
     * @param descripcion Descripción legible del cambio
     * @param resultado   EXITOSO, FALLIDO o DENEGADO
     * @param metodoHttp  Verbo HTTP (GET, POST, PUT, DELETE)
     * @param urlSolicitada URI de la petición
     * @param duracionMs  Duración de la operación en milisegundos (puede ser null)
     * @param mensajeError Mensaje de error si resultado != EXITOSO (puede ser null)
     */
    public void registrar(String ipOrigen, String userAgent, String sesionId,
                          String usuario, String accion, String entidad,
                          String entidadId, String descripcion, String resultado,
                          String metodoHttp, String urlSolicitada,
                          Integer duracionMs, String mensajeError) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, usuario != null ? usuario : "SISTEMA");
            stmt.setString(3, accion);
            stmt.setString(4, entidad);
            stmt.setString(5, entidadId);
            stmt.setString(6, truncar(descripcion, 5000));
            stmt.setString(7, truncar(ipOrigen, 45));
            stmt.setString(8, truncar(userAgent, 500));
            stmt.setString(9, truncar(sesionId, 100));
            stmt.setString(10, resultado != null ? resultado : "EXITOSO");
            stmt.setString(11, truncar(metodoHttp, 10));
            stmt.setString(12, truncar(urlSolicitada, 500));
            if (duracionMs != null) {
                stmt.setInt(13, duracionMs);
            } else {
                stmt.setNull(13, java.sql.Types.INTEGER);
            }
            stmt.setString(14, truncar(mensajeError, 5000));

            stmt.executeUpdate();
            AppLogger.debug("Auditoría registrada: [" + usuario + "] " + accion + " " + entidad
                    + (entidadId != null ? " #" + entidadId : "") + " - " + resultado);

        } catch (SQLException e) {
            // NUNCA propagar el error: la auditoría no debe interrumpir el flujo de negocio
            AppLogger.error("Error al registrar auditoría [usuario=" + usuario
                    + ", accion=" + accion + ", entidad=" + entidad + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Registra una acción de auditoría básica (sin campos de request HTTP).
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
        registrar(ipOrigen, null, null, usuario, accion, entidad, entidadId,
                  descripcion, "EXITOSO", null, null, null, null);
    }

    /**
     * Sobrecarga conveniente que acepta directamente HttpServletRequest para extraer la IP
     * y demás metadatos HTTP.
     */
    public void registrar(HttpServletRequest request, String usuario,
                          String accion, String entidad, String entidadId, String descripcion) {
        if (request == null) {
            registrar((String) null, usuario, accion, entidad, entidadId, descripcion);
            return;
        }
        registrar(
            obtenerIpCliente(request),
            truncar(request.getHeader("User-Agent"), 500),
            obtenerSesionId(request),
            usuario, accion, entidad, entidadId, descripcion,
            "EXITOSO",
            request.getMethod(),
            truncar(request.getRequestURI(), 500),
            null, null
        );
    }

    /**
     * Registra una operación exitosa extrayendo todos los metadatos del request.
     */
    public void registrarExito(HttpServletRequest request, String usuario,
                               String accion, String entidad, String entidadId,
                               String descripcion) {
        if (request == null) {
            registrar((String) null, usuario, accion, entidad, entidadId, descripcion);
            return;
        }
        registrar(
            obtenerIpCliente(request),
            truncar(request.getHeader("User-Agent"), 500),
            obtenerSesionId(request),
            usuario, accion, entidad, entidadId, descripcion,
            "EXITOSO",
            request.getMethod(),
            truncar(request.getRequestURI(), 500),
            null, null
        );
    }

    /**
     * Registra una operación fallida (error de negocio o excepción técnica).
     */
    public void registrarFallo(HttpServletRequest request, String usuario,
                               String accion, String entidad, String entidadId,
                               String descripcion, Exception causa) {
        String mensajeError = causa != null ? causa.getMessage() : null;
        if (request == null) {
            registrar((String) null, null, null, usuario, accion, entidad, entidadId,
                      descripcion, "FALLIDO", null, null, null, mensajeError);
            return;
        }
        registrar(
            obtenerIpCliente(request),
            truncar(request.getHeader("User-Agent"), 500),
            obtenerSesionId(request),
            usuario, accion, entidad, entidadId, descripcion,
            "FALLIDO",
            request.getMethod(),
            truncar(request.getRequestURI(), 500),
            null, truncar(mensajeError, 5000)
        );
    }

    /**
     * Registra un intento de acceso denegado por falta de permisos.
     */
    public void registrarDenegado(HttpServletRequest request, String usuario,
                                  String accion, String entidad, String entidadId,
                                  String descripcion) {
        if (request == null) {
            registrar((String) null, null, null, usuario, accion, entidad, entidadId,
                      descripcion, "DENEGADO", null, null, null, null);
            return;
        }
        registrar(
            obtenerIpCliente(request),
            truncar(request.getHeader("User-Agent"), 500),
            obtenerSesionId(request),
            usuario, accion, entidad, entidadId, descripcion,
            "DENEGADO",
            request.getMethod(),
            truncar(request.getRequestURI(), 500),
            null, null
        );
    }

    /**
     * Extrae la IP real del cliente teniendo en cuenta cabeceras de proxy/balanceador.
     * Soporta X-Forwarded-For, X-Real-IP y RemoteAddr como fallback.
     */
    String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For puede contener varias IPs separadas por coma; la primera es la del cliente original
            int coma = ip.indexOf(',');
            return truncar(coma > 0 ? ip.substring(0, coma).trim() : ip.trim(), 45);
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return truncar(ip.trim(), 45);
        }
        return truncar(request.getRemoteAddr(), 45);
    }

    /**
     * Obtiene el ID de sesión HTTP actual sin crear una nueva sesión.
     */
    private String obtenerSesionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? truncar(session.getId(), 100) : null;
    }

    /**
     * Trunca un String al número máximo de caracteres indicado.
     */
    private String truncar(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}

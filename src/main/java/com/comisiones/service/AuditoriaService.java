package com.comisiones.service;

import com.comisiones.dao.AuditoriaDAO;
import com.comisiones.util.AppLogger;
import com.comisiones.util.DBUtil;

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
        " user_agent, resultado, duracion_ms, mensaje_error, sesion_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
     * @param accion      Tipo: CREAR, MODIFICAR, ELIMINAR, BAJA, LOGIN, LOGOUT, LOGIN_FALLIDO, ACCESS_DENIED
     * @param entidad     Entidad afectada: COMISION, MIEMBRO, ACTA, CARGO, SESION
     * @param entidadId   ID del registro afectado (puede ser compuesto)
     * @param descripcion Descripción legible del cambio
     * @param resultado   Resultado de la operación (EXITOSO, FALLIDO, DENEGADO, VALIDACION_ERROR)
     * @param duracionMs  Duración de la operación en milisegundos (puede ser null)
     * @param mensajeError Detalle del error si la operación falló (puede ser null)
     */
    public void registrar(String ipOrigen, String userAgent, String sesionId,
                          String usuario, String accion,
                          String entidad, String entidadId, String descripcion,
                          AuditoriaDAO.Resultado resultado, Integer duracionMs, String mensajeError) {
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
            stmt.setString(9, resultado != null ? resultado.name() : null);
            if (duracionMs != null) {
                stmt.setInt(10, duracionMs);
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }
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
     * Registra una acción de auditoría básica (sin metadatos extendidos).
     * Los campos user_agent, sesion_id, resultado, duracion_ms y mensaje_error
     * se almacenarán como NULL.
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
                AuditoriaDAO.Resultado.EXITOSO, null, null);
    }

    /**
     * Sobrecarga conveniente que acepta directamente HttpServletRequest para extraer
     * la IP real (soporta cabeceras de proxy), el User-Agent y el ID de sesión.
     * El resultado se registra como EXITOSO.
     */
    public void registrar(javax.servlet.http.HttpServletRequest request, String usuario,
                          String accion, String entidad, String entidadId, String descripcion) {
        String ip        = obtenerIPReal(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String sesionId  = request != null ? obtenerSesionId(request) : null;
        registrar(ip, userAgent, sesionId, usuario, accion, entidad, entidadId, descripcion,
                AuditoriaDAO.Resultado.EXITOSO, null, null);
    }

    /**
     * Registra una operación con resultado explícito, extrayendo metadatos HTTP del request.
     *
     * @param request      petición HTTP (puede ser null)
     * @param usuario      nombre de usuario
     * @param accion       tipo de acción
     * @param entidad      entidad afectada
     * @param entidadId    ID del registro afectado
     * @param descripcion  descripción del cambio
     * @param resultado    resultado de la operación
     * @param duracionMs   duración en milisegundos (puede ser null)
     * @param mensajeError mensaje de error en caso de fallo (puede ser null)
     */
    public void registrarConResultado(javax.servlet.http.HttpServletRequest request, String usuario,
                                      String accion, String entidad, String entidadId, String descripcion,
                                      AuditoriaDAO.Resultado resultado, Integer duracionMs, String mensajeError) {
        String ip        = obtenerIPReal(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String sesionId  = request != null ? obtenerSesionId(request) : null;
        registrar(ip, userAgent, sesionId, usuario, accion, entidad, entidadId, descripcion,
                resultado, duracionMs, mensajeError);
    }

    /**
     * Extrae la IP real del cliente teniendo en cuenta cabeceras de proxy estándar.
     * Orden de precedencia: X-Forwarded-For → X-Real-IP → RemoteAddr.
     */
    private String obtenerIPReal(javax.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For puede ser una lista separada por comas: client, proxy1, proxy2
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Obtiene el ID de sesión HTTP sin crear una nueva sesión si no existe.
     */
    private String obtenerSesionId(javax.servlet.http.HttpServletRequest request) {
        javax.servlet.http.HttpSession session = request.getSession(false);
        return session != null ? session.getId() : null;
    }
}


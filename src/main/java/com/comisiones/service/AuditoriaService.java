package com.comisiones.service;

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
        "INSERT INTO auditoria_acciones (fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

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
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, usuario != null ? usuario : "SISTEMA");
            stmt.setString(3, accion);
            stmt.setString(4, entidad);
            stmt.setString(5, entidadId);
            stmt.setString(6, descripcion);
            stmt.setString(7, ipOrigen);

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
    public void registrar(javax.servlet.http.HttpServletRequest request, String usuario,
                          String accion, String entidad, String entidadId, String descripcion) {
        String ip = request != null ? request.getRemoteAddr() : null;
        registrar(ip, usuario, accion, entidad, entidadId, descripcion);
    }
}

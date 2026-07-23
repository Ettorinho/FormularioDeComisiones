package com.comisiones.dao;

import com.comisiones.model.AuditoriaAccion;
import com.comisiones.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de solo lectura para consultar el historial de auditoría.
 */
public class AuditoriaDAO {

    /**
     * Posibles resultados de una operación auditada.
     */
    public enum Resultado {
        EXITOSO, FALLIDO, DENEGADO, VALIDACION_ERROR
    }

    private static final String COLS =
            "id, fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen," +
            " user_agent, resultado, duracion_ms, mensaje_error, sesion_id";

    private static final String FIND_ALL_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones ORDER BY fecha_hora DESC LIMIT ?";

    private static final String FIND_BY_USUARIO_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones WHERE usuario = ? ORDER BY fecha_hora DESC";

    private static final String FIND_BY_ENTIDAD_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones WHERE entidad = ? AND entidad_id = ? ORDER BY fecha_hora DESC";

    private static final String FIND_BY_RESULTADO_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones WHERE resultado = ? ORDER BY fecha_hora DESC LIMIT ?";

    private static final String FIND_INTENTOS_FALLIDOS_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones" +
            " WHERE usuario = ? AND resultado IN ('FALLIDO', 'DENEGADO')" +
            "   AND fecha_hora > CURRENT_TIMESTAMP - (? * INTERVAL '1 day')" +
            " ORDER BY fecha_hora DESC LIMIT 100";

    private static final String FIND_BY_IP_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones" +
            " WHERE ip_origen = ? AND fecha_hora > CURRENT_TIMESTAMP - (? * INTERVAL '1 hour')" +
            " ORDER BY fecha_hora DESC LIMIT 100";

    public List<AuditoriaAccion> findAll(int limit) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    public List<AuditoriaAccion> findByUsuario(String usuario) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USUARIO_SQL)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    public List<AuditoriaAccion> findByEntidad(String entidad, String entidadId) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ENTIDAD_SQL)) {
            stmt.setString(1, entidad);
            stmt.setString(2, entidadId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    /**
     * Busca registros filtrados por resultado (EXITOSO, FALLIDO, DENEGADO, VALIDACION_ERROR).
     *
     * @param resultado valor del enum a filtrar
     * @param limit     máximo número de registros a devolver
     */
    public List<AuditoriaAccion> findByResultado(Resultado resultado, int limit) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_RESULTADO_SQL)) {
            stmt.setString(1, resultado.name());
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    /**
     * Devuelve los intentos fallidos o denegados de un usuario en los últimos {@code dias} días.
     *
     * @param usuario nombre de usuario AD
     * @param dias    número de días hacia atrás a consultar
     */
    public List<AuditoriaAccion> findIntentosFallidos(String usuario, int dias) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_INTENTOS_FALLIDOS_SQL)) {
            stmt.setString(1, usuario);
            stmt.setInt(2, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    /**
     * Devuelve las acciones registradas desde una IP en las últimas {@code horas} horas.
     *
     * @param ipOrigen dirección IP a consultar
     * @param horas    número de horas hacia atrás a consultar
     */
    public List<AuditoriaAccion> findByIp(String ipOrigen, int horas) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_IP_SQL)) {
            stmt.setString(1, ipOrigen);
            stmt.setInt(2, horas);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    private AuditoriaAccion extract(ResultSet rs) throws SQLException {
        AuditoriaAccion a = new AuditoriaAccion();
        a.setId(rs.getLong("id"));
        a.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        a.setUsuario(rs.getString("usuario"));
        a.setAccion(rs.getString("accion"));
        a.setEntidad(rs.getString("entidad"));
        a.setEntidadId(rs.getString("entidad_id"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setIpOrigen(rs.getString("ip_origen"));
        a.setUserAgent(rs.getString("user_agent"));
        a.setResultado(rs.getString("resultado"));
        int dur = rs.getInt("duracion_ms");
        a.setDuracionMs(rs.wasNull() ? null : dur);
        a.setMensajeError(rs.getString("mensaje_error"));
        a.setSesionId(rs.getString("sesion_id"));
        return a;
    }
}

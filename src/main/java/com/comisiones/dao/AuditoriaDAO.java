package com.comisiones.dao;

import com.comisiones.model.AuditoriaAccion;
import com.comisiones.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de solo lectura para consultar el historial de auditoría.
 */
public class AuditoriaDAO {

    /**
     * Posibles resultados de una operación auditada.
     */
    public enum Resultado {
        EXITOSO, FALLIDO, DENEGADO
    }

    private static final String COLS =
            "id, fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen," +
            " user_agent, session_id, resultado, metodo_http, url_solicitada, duracion_ms, mensaje_error";

    private static final String FIND_ALL_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones ORDER BY fecha_hora DESC LIMIT ?";

    private static final String FIND_BY_USUARIO_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones WHERE usuario = ? ORDER BY fecha_hora DESC";

    private static final String FIND_BY_ENTIDAD_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones" +
            " WHERE entidad = ? AND entidad_id = ? ORDER BY fecha_hora DESC";

    private static final String FIND_BY_RESULTADO_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones WHERE resultado = ? ORDER BY fecha_hora DESC LIMIT ?";

    private static final String FIND_DENEGADOS_SQL =
            "SELECT " + COLS + " FROM auditoria_acciones" +
            " WHERE resultado = 'DENEGADO' AND fecha_hora > NOW() - (? * INTERVAL '1 hour')" +
            " ORDER BY fecha_hora DESC LIMIT 100";

    private static final String ACTIVIDAD_POR_IP_SQL =
            "SELECT ip_origen, COUNT(*) AS total FROM auditoria_acciones" +
            " WHERE fecha_hora > NOW() - (? * INTERVAL '1 day')" +
            " GROUP BY ip_origen ORDER BY total DESC LIMIT 20";

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
     * Devuelve registros filtrados por resultado (EXITOSO, FALLIDO o DENEGADO).
     *
     * @param resultado valor del campo resultado
     * @param limit     número máximo de registros a devolver
     */
    public List<AuditoriaAccion> findByResultado(String resultado, int limit) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_RESULTADO_SQL)) {
            stmt.setString(1, resultado);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    /**
     * Devuelve los intentos de acceso denegados en las últimas {@code ultimasHoras} horas.
     *
     * @param ultimasHoras número de horas hacia atrás a consultar
     */
    public List<AuditoriaAccion> obtenerIntentosDenegados(int ultimasHoras) throws SQLException {
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_DENEGADOS_SQL)) {
            stmt.setInt(1, ultimasHoras);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    /**
     * Devuelve un mapa IP → número de acciones en los últimos {@code ultimosDias} días,
     * ordenado de mayor a menor actividad (máximo 20 IPs).
     *
     * @param ultimosDias número de días hacia atrás a consultar
     */
    public Map<String, Long> obtenerActividadPorIp(int ultimosDias) throws SQLException {
        Map<String, Long> resultado = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ACTIVIDAD_POR_IP_SQL)) {
            stmt.setInt(1, ultimosDias);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString("ip_origen"), rs.getLong("total"));
                }
            }
        }
        return resultado;
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
        a.setSessionId(rs.getString("session_id"));
        a.setResultado(rs.getString("resultado"));
        a.setMetodoHttp(rs.getString("metodo_http"));
        a.setUrlSolicitada(rs.getString("url_solicitada"));
        int dur = rs.getInt("duracion_ms");
        a.setDuracionMs(rs.wasNull() ? null : dur);
        a.setMensajeError(rs.getString("mensaje_error"));
        return a;
    }
}

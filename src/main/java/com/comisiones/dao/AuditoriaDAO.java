package com.comisiones.dao;

import com.comisiones.model.AuditoriaAccion;
import com.comisiones.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de solo lectura para consultar el historial de auditoría.
 */
public class AuditoriaDAO {

    private static final String BASE_SELECT = String.join(" ",
            "SELECT id, fecha_hora, usuario, accion, entidad, entidad_id, descripcion,",
            "ip_origen, user_agent, resultado, duracion_ms, mensaje_error, sesion_id",
            "FROM auditoria_acciones");

    public List<AuditoriaAccion> findAll(int limit) throws SQLException {
        String sql = String.join(" ", BASE_SELECT, "ORDER BY fecha_hora DESC LIMIT ?");
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    public List<AuditoriaAccion> findByUsuario(String usuario) throws SQLException {
        String sql = String.join(" ", BASE_SELECT, "WHERE usuario = ? ORDER BY fecha_hora DESC");
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    public List<AuditoriaAccion> findByEntidad(String entidad, String entidadId) throws SQLException {
        String sql = String.join(" ", BASE_SELECT,
                "WHERE entidad = ? AND entidad_id = ? ORDER BY fecha_hora DESC");
        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entidad);
            stmt.setString(2, entidadId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(extract(rs));
            }
        }
        return lista;
    }

    public List<AuditoriaAccion> findByFiltros(String usuario, String resultado,
                                               LocalDate fechaDesde, LocalDate fechaHasta,
                                               String ipAddress, int limit) throws SQLException {
        List<String> condiciones = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (usuario != null && !usuario.trim().isEmpty()) {
            condiciones.add("usuario = ?");
            params.add(usuario.trim());
        }
        if (resultado != null && !resultado.trim().isEmpty()) {
            condiciones.add("resultado = ?");
            params.add(resultado.trim());
        }
        if (fechaDesde != null) {
            condiciones.add("fecha_hora >= ?");
            params.add(Timestamp.valueOf(fechaDesde.atStartOfDay()));
        }
        if (fechaHasta != null) {
            condiciones.add("fecha_hora < ?");
            params.add(Timestamp.valueOf(fechaHasta.plusDays(1).atStartOfDay()));
        }
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            condiciones.add("ip_origen = ?");
            params.add(ipAddress.trim());
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT);
        if (!condiciones.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", condiciones));
        }
        sql.append(" ORDER BY fecha_hora DESC LIMIT ?");

        List<AuditoriaAccion> lista = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object param : params) {
                stmt.setObject(index++, param);
            }
            stmt.setInt(index, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extract(rs));
                }
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
        a.setDuracionMs((Integer) rs.getObject("duracion_ms"));
        a.setMensajeError(rs.getString("mensaje_error"));
        a.setSesionId(rs.getString("sesion_id"));
        return a;
    }
}

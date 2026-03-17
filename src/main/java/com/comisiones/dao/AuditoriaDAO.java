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

    public List<AuditoriaAccion> findAll(int limit) throws SQLException {
        String sql = "SELECT id, fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen " +
                     "FROM auditoria_acciones ORDER BY fecha_hora DESC LIMIT ?";
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
        String sql = "SELECT id, fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen " +
                     "FROM auditoria_acciones WHERE usuario = ? ORDER BY fecha_hora DESC";
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
        String sql = "SELECT id, fecha_hora, usuario, accion, entidad, entidad_id, descripcion, ip_origen " +
                     "FROM auditoria_acciones WHERE entidad = ? AND entidad_id = ? ORDER BY fecha_hora DESC";
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
        return a;
    }
}

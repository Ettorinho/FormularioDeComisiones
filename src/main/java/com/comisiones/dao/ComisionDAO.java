package com.comisiones.dao;

import com.comisiones.model.Comision;
import com.comisiones.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComisionDAO {
    // Tu código de ComisionDAO es correcto, solo asegúrate de que el package sea com.comisiones.dao
    // y los imports apunten a com.comisiones.model
    public void save(Comision comision) throws SQLException {
        String sql = "INSERT INTO comisiones (nombre, fecha_constitucion, fecha_fin) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, comision.getNombre());
            stmt.setDate(2, new java.sql.Date(comision.getFechaConstitucion().getTime()));
            if (comision.getFechaFin() != null) {
                stmt.setDate(3, new java.sql.Date(comision.getFechaFin().getTime()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comision.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Comision findById(Long id) throws SQLException {
        String sql = "SELECT * FROM comisiones WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractComisionFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Comision> findAll() throws SQLException {
        List<Comision> comisiones = new ArrayList<>();
        String sql = "SELECT * FROM comisiones ORDER BY nombre";
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comisiones.add(extractComisionFromResultSet(rs));
            }
        }
        return comisiones;
    }

    public boolean exists(String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comisiones WHERE nombre = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Comision extractComisionFromResultSet(ResultSet rs) throws SQLException {
        Comision comision = new Comision();
        comision.setId(rs.getLong("id"));
        comision.setNombre(rs.getString("nombre"));
        comision.setFechaConstitucion(rs.getDate("fecha_constitucion"));
        comision.setFechaFin(rs.getDate("fecha_fin"));
        comision.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        return comision;
    }
}
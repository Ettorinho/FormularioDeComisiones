package com.comisiones.dao;

import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;
import com.comisiones.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComisionMiembroDAO {

    private ComisionDAO comisionDAO = new ComisionDAO();
    private MiembroDAO miembroDAO = new MiembroDAO();

    public void save(ComisionMiembro comisionMiembro) throws SQLException {
        String sql = "INSERT INTO comision_miembros (comision_id, miembro_id, cargo, fecha_incorporacion) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, comisionMiembro.getComision().getId());
            stmt.setLong(2, comisionMiembro.getMiembro().getId());
            stmt.setString(3, comisionMiembro.getCargo().name());
            stmt.setDate(4, new java.sql.Date(comisionMiembro.getFechaIncorporacion().getTime()));
            stmt.executeUpdate();
        }
    }

    public void darDeBaja(Long comisionId, Long miembroId, Date fechaBaja) throws SQLException {
        String sql = "UPDATE comision_miembros SET fecha_baja = ? WHERE comision_id = ? AND miembro_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, new java.sql.Date(fechaBaja.getTime()));
            stmt.setLong(2, comisionId);
            stmt.setLong(3, miembroId);
            stmt.executeUpdate();
        }
    }

    public List<ComisionMiembro> findByComisionId(Long comisionId) throws SQLException {
        List<ComisionMiembro> result = new ArrayList<>();
        String sql = "SELECT cm.*, m.nombre_apellidos, m.dni_nif, m.correo_electronico FROM comision_miembros cm JOIN miembros m ON cm.miembro_id = m.id WHERE cm.comision_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, comisionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(extractComisionMiembroWithMiembroFromResultSet(rs));
                }
            }
        }
        return result;
    }

    public List<ComisionMiembro> findByMiembroId(Long miembroId) throws SQLException {
        List<ComisionMiembro> result = new ArrayList<>();
        String sql = "SELECT cm.*, c.nombre, c.fecha_constitucion, c.fecha_fin FROM comision_miembros cm JOIN comisiones c ON cm.comision_id = c.id WHERE cm.miembro_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, miembroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comision comision = new Comision();
                    comision.setId(rs.getLong("comision_id"));
                    comision.setNombre(rs.getString("nombre"));
                    comision.setFechaConstitucion(rs.getDate("fecha_constitucion"));
                    comision.setFechaFin(rs.getDate("fecha_fin"));
                    ComisionMiembro comisionMiembro = new ComisionMiembro();
                    comisionMiembro.setComision(comision);
                    comisionMiembro.setCargo(ComisionMiembro.Cargo.valueOf(rs.getString("cargo")));
                    comisionMiembro.setFechaIncorporacion(rs.getDate("fecha_incorporacion"));
                    comisionMiembro.setFechaBaja(rs.getDate("fecha_baja"));
                    result.add(comisionMiembro);
                }
            }
        }
        return result;
    }

    private ComisionMiembro extractComisionMiembroWithMiembroFromResultSet(ResultSet rs) throws SQLException {
        Miembro miembro = new Miembro();
        miembro.setId(rs.getLong("miembro_id"));
        miembro.setNombreApellidos(rs.getString("nombre_apellidos"));
        miembro.setDniNif(rs.getString("dni_nif"));
        miembro.setEmail(rs.getString("correo_electronico"));

        ComisionMiembro comisionMiembro = new ComisionMiembro();
        comisionMiembro.setMiembro(miembro);
        comisionMiembro.setCargo(ComisionMiembro.Cargo.valueOf(rs.getString("cargo")));
        comisionMiembro.setFechaIncorporacion(rs.getDate("fecha_incorporacion"));
        comisionMiembro.setFechaBaja(rs.getDate("fecha_baja"));
        // No establecemos la comisión aquí para evitar bucles infinitos
        return comisionMiembro;
    }
}
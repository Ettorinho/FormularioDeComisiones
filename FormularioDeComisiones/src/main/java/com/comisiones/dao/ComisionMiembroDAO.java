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
        
        // No establecemos la comisión aquí para evitar bucles infinitos
        
        return comisionMiembro;
    }
}
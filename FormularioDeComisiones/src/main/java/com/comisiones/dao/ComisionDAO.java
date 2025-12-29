package com.comisiones.dao;

import com.comisiones.model.Comision;
import com.comisiones.model. Comision. Area;
import com.comisiones.model.Comision. Tipo;
import com.comisiones.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComisionDAO {
    
    public void save(Comision comision) throws SQLException {
        System.out.println("===> [DEBUG] ComisionDAO.save: " + comision.getNombre());
        String sql = "INSERT INTO comisiones (nombre, area, tipo, fecha_constitucion, fecha_fin) VALUES (?, ? ::area_type, ? ::tipo_type, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, comision.getNombre());
            stmt.setString(2, comision.getArea().name());
            stmt.setString(3, comision.getTipo().name());
            stmt.setDate(4, new java.sql.Date(comision.getFechaConstitucion().getTime()));
            
            if (comision.getFechaFin() != null) {
                stmt. setDate(5, new java. sql.Date(comision.getFechaFin().getTime()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comision. setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public Comision findById(Long id) throws SQLException {
        String sql = "SELECT * FROM comisiones WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT * FROM comisiones ORDER BY area, tipo, nombre";
        try (Connection conn = DBUtil.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comisiones.add(extractComisionFromResultSet(rs));
            }
        }
        return comisiones;
    }
    
    // ⭐ NUEVO:  Buscar por área y tipo
    public List<Comision> findByAreaAndTipo(Area area, Tipo tipo) throws SQLException {
        List<Comision> comisiones = new ArrayList<>();
        String sql = "SELECT * FROM comisiones WHERE area = ? ::area_type AND tipo = ? ::tipo_type ORDER BY nombre";
        
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, area.name());
            stmt.setString(2, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comisiones.add(extractComisionFromResultSet(rs));
                }
            }
        }
        return comisiones;
    }
    
    public List<Comision> findByNombreLike(String nombre) throws SQLException {
        List<Comision> comisiones = new ArrayList<>();
        String sql = "SELECT * FROM comisiones WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nombre + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comisiones.add(extractComisionFromResultSet(rs));
                }
            }
        }
        return comisiones;
    }
    
    public boolean exists(String nombre, Area area, Tipo tipo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comisiones WHERE nombre = ? AND area = ? ::area_type AND tipo = ? ::tipo_type";
        try (Connection conn = DBUtil.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, area.name());
            stmt.setString(3, tipo.name());
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
        
        // Convertir String del DB a Enum
        String areaStr = rs.getString("area");
        if (areaStr != null) {
            comision.setArea(Area.valueOf(areaStr));
        }
        
        String tipoStr = rs.getString("tipo");
        if (tipoStr != null) {
            comision.setTipo(Tipo.valueOf(tipoStr));
        }
        
        comision.setFechaConstitucion(rs.getDate("fecha_constitucion"));
        comision.setFechaFin(rs.getDate("fecha_fin"));
        comision.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        
        return comision;
    }
}
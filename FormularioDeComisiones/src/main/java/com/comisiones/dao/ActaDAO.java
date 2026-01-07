package com.comisiones. dao;

import com.comisiones.model.Acta;
import com.comisiones. model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model. Miembro;
import com.comisiones.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActaDAO {
    
    public void save(Acta acta) throws SQLException {
        String sql = "INSERT INTO actas (comision_id, fecha_reunion, observaciones) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, acta.getComision().getId());
            stmt.setDate(2, new java.sql.Date(acta. getFechaReunion().getTime()));
            stmt.setString(3, acta.getObservaciones());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    acta.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public void saveAsistencia(AsistenciaActa asistencia) throws SQLException {
        String sql = "INSERT INTO asistencias_acta (acta_id, miembro_id, asistio) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, asistencia.getActa().getId());
            stmt.setLong(2, asistencia. getMiembro().getId());
            stmt.setBoolean(3, asistencia.isAsistio());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt. getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    asistencia. setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public Acta findById(Long id) throws SQLException {
        String sql = "SELECT a.*, c.nombre as comision_nombre, c.area, c.tipo " +
                     "FROM actas a " +
                     "INNER JOIN comisiones c ON a.comision_id = c. id " +
                     "WHERE a.id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractActaFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public List<Acta> findByComisionId(Long comisionId) throws SQLException {
        List<Acta> actas = new ArrayList<>();
        String sql = "SELECT a.*, c.nombre as comision_nombre, c.area, c.tipo " +
                     "FROM actas a " +
                     "INNER JOIN comisiones c ON a.comision_id = c. id " +
                     "WHERE a.comision_id = ?  " +
                     "ORDER BY a.fecha_reunion DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, comisionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    actas.add(extractActaFromResultSet(rs));
                }
            }
        }
        return actas;
    }
    
    public List<Acta> findAll() throws SQLException {
        List<Acta> actas = new ArrayList<>();
        String sql = "SELECT a.*, c.nombre as comision_nombre, c. area, c.tipo " +
                     "FROM actas a " +
                     "INNER JOIN comisiones c ON a.comision_id = c.id " +
                     "ORDER BY a.fecha_reunion DESC";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt. executeQuery(sql)) {
            
            while (rs.next()) {
                actas.add(extractActaFromResultSet(rs));
            }
        }
        return actas;
    }
    
    public List<AsistenciaActa> findAsistenciasByActaId(Long actaId) throws SQLException {
        List<AsistenciaActa> asistencias = new ArrayList<>();
        String sql = "SELECT aa.*, m.nombre_apellidos, m.dni_nif, m.correo_electronico " +
                     "FROM asistencias_acta aa " +
                     "INNER JOIN miembros m ON aa.miembro_id = m.id " +
                     "WHERE aa. acta_id = ? " +
                     "ORDER BY m.nombre_apellidos";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, actaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AsistenciaActa asistencia = new AsistenciaActa();
                    asistencia.setId(rs.getLong("id"));
                    asistencia. setAsistio(rs.getBoolean("asistio"));
                    
                    Miembro miembro = new Miembro();
                    miembro.setId(rs.getLong("miembro_id"));
                    miembro.setNombreApellidos(rs.getString("nombre_apellidos"));
                    miembro.setDniNif(rs.getString("dni_nif"));
                    miembro.setEmail(rs.getString("correo_electronico"));
                    
                    asistencia.setMiembro(miembro);
                    asistencias.add(asistencia);
                }
            }
        }
        return asistencias;
    }
    
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM actas WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    private Acta extractActaFromResultSet(ResultSet rs) throws SQLException {
        Acta acta = new Acta();
        acta.setId(rs. getLong("id"));
        acta.setFechaReunion(rs.getDate("fecha_reunion"));
        acta.setObservaciones(rs.getString("observaciones"));
        acta.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        
        Comision comision = new Comision();
        comision.setId(rs.getLong("comision_id"));
        comision.setNombre(rs.getString("comision_nombre"));
        
        try {
            String areaStr = rs.getString("area");
            if (areaStr != null) {
                comision.setArea(Comision.Area.valueOf(areaStr));
            }
        } catch (SQLException e) {
            // Campo no disponible
        }
        
        try {
            String tipoStr = rs.getString("tipo");
            if (tipoStr != null) {
                comision.setTipo(Comision.Tipo.valueOf(tipoStr));
            }
        } catch (SQLException e) {
            // Campo no disponible
        }
        
        acta.setComision(comision);
        
        return acta;
    }
}
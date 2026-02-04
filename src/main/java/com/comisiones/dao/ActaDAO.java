package com.comisiones.dao;

import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model.Miembro;
import com.comisiones.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActaDAO {
    
    private ComisionDAO comisionDAO = new ComisionDAO();
    
    public void save(Acta acta) {
        // Validaciones
        if (acta == null) {
            throw new IllegalArgumentException("El acta no puede ser null");
        }
        if (acta.getComision() == null) {
            throw new IllegalArgumentException("La comisión del acta no puede ser null");
        }
        if (acta.getComision().getId() == null) {
            throw new IllegalArgumentException("El ID de la comisión no puede ser null");
        }
        
        String sql = "INSERT INTO actas (comision_id, fecha_reunion, observaciones, fecha_creacion) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, acta.getComision().getId());
            stmt.setDate(2, new java.sql.Date(acta.getFechaReunion().getTime()));
            stmt.setString(3, acta.getObservaciones());
            stmt.setTimestamp(4, new Timestamp(acta.getFechaCreacion().getTime()));
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                acta.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al guardar acta", e);
        }
    }
    
    public void saveAsistencia(AsistenciaActa asistencia) {
        System.out.println(">>> ActaDAO.saveAsistencia() INICIADO");
        System.out.println("    Acta ID: " + (asistencia.getActa() != null ? asistencia.getActa().getId() : "NULL"));
        System.out.println("    Miembro ID: " + (asistencia.getMiembro() != null ? asistencia.getMiembro().getId() : "NULL"));
        System.out.println("    Asistió: " + asistencia.isAsistio());
        System.out.println("    Justificación: " + (asistencia.getJustificacion() != null ? "'" + asistencia.getJustificacion() + "'" : "NULL"));
        
        String sql = "INSERT INTO asistencias_acta (acta_id, miembro_id, asistio, justificacion, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        System.out.println("    SQL: " + sql);
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, asistencia.getActa().getId());
            stmt.setLong(2, asistencia.getMiembro().getId());
            stmt.setBoolean(3, asistencia.isAsistio());
            stmt.setString(4, asistencia.getJustificacion());
            stmt.setTimestamp(5, new Timestamp(asistencia.getFechaCreacion().getTime()));
            
            System.out.println("    Parámetros preparados:");
            System.out.println("      1: " + asistencia.getActa().getId());
            System.out.println("      2: " + asistencia.getMiembro().getId());
            System.out.println("      3: " + asistencia.isAsistio());
            System.out.println("      4: " + asistencia.getJustificacion());
            System.out.println("      5: " + asistencia.getFechaCreacion());
            
            int rows = stmt.executeUpdate();
            System.out.println("    Filas insertadas: " + rows);
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                asistencia.setId(rs.getLong(1));
                System.out.println("    ✅ ID generado: " + asistencia.getId());
            }
            
            System.out.println(">>> ActaDAO.saveAsistencia() COMPLETADO\n");
            
        } catch (SQLException e) {
            System.err.println(">>> ❌ ERROR en ActaDAO.saveAsistencia():");
            e.printStackTrace();
            throw new RuntimeException("Error al guardar asistencia", e);
        }
    }
    
    public Acta findById(Long id) throws SQLException {
        String sql = "SELECT id, comision_id, fecha_reunion, observaciones, fecha_creacion " +
                     "FROM actas WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Acta acta = new Acta();
                acta.setId(rs.getLong("id"));
                
                Long comisionId = rs.getLong("comision_id");
                Comision comision = comisionDAO.findById(comisionId);
                acta.setComision(comision);
                
                acta.setFechaReunion(rs.getDate("fecha_reunion"));
                acta.setObservaciones(rs.getString("observaciones"));
                acta.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                
                return acta;
            }
        }
        
        return null;
    }
    
    public List<AsistenciaActa> findAsistenciasByActaId(Long actaId) throws SQLException {
        List<AsistenciaActa> asistencias = new ArrayList<>();
        
        String sql = "SELECT aa.id, aa.acta_id, aa.miembro_id, aa.asistio, aa.justificacion, aa.fecha_creacion, " +
                     "m.nombre_apellidos, m.dni_nif, m.correo_electronico " +
                     "FROM asistencias_acta aa " +
                     "INNER JOIN miembros m ON aa.miembro_id = m.id " +
                     "WHERE aa.acta_id = ? " +
                     "ORDER BY m.nombre_apellidos";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, actaId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Crear objeto Miembro
                Miembro miembro = new Miembro();
                miembro.setId(rs.getLong("miembro_id"));
                miembro.setNombreApellidos(rs.getString("nombre_apellidos"));
                miembro.setDniNif(rs.getString("dni_nif"));
                
                // Crear objeto AsistenciaActa
                AsistenciaActa asistencia = new AsistenciaActa();
                asistencia.setId(rs.getLong("id"));
                asistencia.setMiembro(miembro);
                asistencia.setAsistio(rs.getBoolean("asistio"));
                asistencia.setJustificacion(rs.getString("justificacion"));
                asistencia.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                
                asistencias.add(asistencia);
            }
        }
        
        return asistencias;
    }
    
    public List<Acta> findAll() throws SQLException {
        List<Acta> actas = new ArrayList<>();
        
        String sql = "SELECT id, comision_id, fecha_reunion, observaciones, fecha_creacion " +
                     "FROM actas ORDER BY fecha_reunion DESC";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Acta acta = new Acta();
                acta.setId(rs.getLong("id"));
                
                Long comisionId = rs.getLong("comision_id");
                Comision comision = comisionDAO.findById(comisionId);
                acta.setComision(comision);
                
                acta.setFechaReunion(rs.getDate("fecha_reunion"));
                acta.setObservaciones(rs.getString("observaciones"));
                acta.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                
                actas.add(acta);
            }
        }
        
        return actas;
    }
}
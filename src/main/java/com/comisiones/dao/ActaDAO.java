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
    
    /**
     * Guarda un acta en la base de datos (incluyendo PDF si existe)
     */
    public Long save(Acta acta) throws SQLException {
        String sql = "INSERT INTO actas (comision_id, fecha_reunion, observaciones, fecha_creacion, " +
                     "pdf_nombre, pdf_contenido, pdf_tipo_mime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        System.out.println("\n>>> ActaDAO.save() INICIADO");
        System.out.println("    Comisión ID: " + acta.getComision().getId());
        System.out.println("    Fecha reunión: " + acta.getFechaReunion());
        System.out.println("    Tiene PDF: " + acta.tienePdf());
        if (acta.tienePdf()) {
            System.out.println("    PDF nombre: " + acta.getPdfNombre());
            System.out.println("    PDF tamaño: " + acta.getPdfContenido().length + " bytes");
        }
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, acta.getComision().getId());
            stmt.setDate(2, new java.sql.Date(acta.getFechaReunion().getTime()));
            stmt.setString(3, acta.getObservaciones());
            stmt.setTimestamp(4, new java.sql.Timestamp(acta.getFechaCreacion().getTime()));
            
            // Campos para PDF
            stmt.setString(5, acta.getPdfNombre());
            stmt.setBytes(6, acta.getPdfContenido());
            stmt.setString(7, acta.getPdfTipoMime());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long generatedId = rs.getLong("id");
                System.out.println("    ✅ Acta guardada con ID: " + generatedId);
                System.out.println(">>> ActaDAO.save() COMPLETADO\n");
                return generatedId;
            }
        }
        
        System.out.println("    ❌ Error: No se generó ID");
        System.out.println(">>> ActaDAO.save() FALLIDO\n");
        return null;
    }
    
    /**
     * Guarda una asistencia de acta
     */
    public Long saveAsistencia(Long actaId, Long miembroId, boolean asistio, String justificacion) 
            throws SQLException {
        
        String sql = "INSERT INTO asistencias_acta (acta_id, miembro_id, asistio, justificacion, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        System.out.println(">>> ActaDAO.saveAsistencia() INICIADO");
        System.out.println("    Acta ID: " + actaId);
        System.out.println("    Miembro ID: " + miembroId);
        System.out.println("    Asistió: " + asistio);
        System.out.println("    Justificación: " + (justificacion != null ? "'" + justificacion + "'" : "NULL"));
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, actaId);
            stmt.setLong(2, miembroId);
            stmt.setBoolean(3, asistio);
            stmt.setString(4, justificacion);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = stmt.executeUpdate();
            System.out.println("    Filas insertadas: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("No se pudo guardar la asistencia");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);
                    System.out.println("    ✅ ID generado: " + generatedId);
                    System.out.println(">>> ActaDAO.saveAsistencia() COMPLETADO\n");
                    return generatedId;
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            System.out.println("    ❌ ERROR SQL: " + e.getMessage());
            System.out.println(">>> ActaDAO.saveAsistencia() FALLIDO\n");
            throw e;
        }
    }
    
    /**
     * Busca un acta por ID (sin cargar el contenido del PDF para ahorrar memoria)
     */
    public Acta findById(Long id) throws SQLException {
        String sql = "SELECT a.id, a.fecha_reunion, a.observaciones, a.fecha_creacion, " +
                     "a.pdf_nombre, a.pdf_tipo_mime, " +
                     "c.id as comision_id, c.nombre as comision_nombre " +
                     "FROM actas a " +
                     "INNER JOIN comisiones c ON a.comision_id = c.id " +
                     "WHERE a.id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Acta acta = new Acta();
                acta.setId(rs.getLong("id"));
                acta.setFechaReunion(rs.getDate("fecha_reunion"));
                acta.setObservaciones(rs.getString("observaciones"));
                acta.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                
                // Información del PDF (sin cargar el contenido)
                acta.setPdfNombre(rs.getString("pdf_nombre"));
                acta.setPdfTipoMime(rs.getString("pdf_tipo_mime"));
                
                // Crear objeto Comision
                Comision comision = new Comision();
                comision.setId(rs.getLong("comision_id"));
                comision.setNombre(rs.getString("comision_nombre"));
                
                acta.setComision(comision);
                
                return acta;
            }
        }
        return null;
    }
    
    /**
     * Obtiene solo el contenido del PDF (para descarga)
     */
    public byte[] getPdfContenido(Long actaId) throws SQLException {
        String sql = "SELECT pdf_contenido FROM actas WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, actaId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBytes("pdf_contenido");
            }
        }
        return null;
    }
    
    /**
     * Busca las asistencias de un acta
     */
    public List<AsistenciaActa> findAsistenciasByActaId(Long actaId) throws SQLException {
        List<AsistenciaActa> asistencias = new ArrayList<>();
        
        String sql = "SELECT aa.id, aa.acta_id, aa.miembro_id, aa.asistio, aa.justificacion, aa.fecha_creacion, " +
                     "m.nombre_apellidos, m.dni_nif " +
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
}
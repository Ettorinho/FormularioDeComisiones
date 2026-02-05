package com.comisiones.dao;

import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model.Miembro;
import com.comisiones.util.DBUtil;
import com.comisiones.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActaDAO {
    
    /**
     * Guarda un acta en la base de datos (incluyendo PDF si existe)
     */
    public Long save(Acta acta) throws SQLException {
        String sql = "INSERT INTO actas (comision_id, fecha_reunion, observaciones, fecha_creacion, " +
                     "pdf_nombre, pdf_contenido, pdf_tipo_mime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        AppLogger.debug("Guardando acta");
        
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
                AppLogger.debug("Acta guardada con ID: " + generatedId);
                return generatedId;
            }
        }
        
        return null;
    }
    
    /**
     * Guarda el acta y retorna el ID generado
     * @param conn Conexión existente (para transacciones)
     */
    private Long saveActa(Connection conn, Acta acta) throws SQLException {
        String sql = "INSERT INTO actas (comision_id, fecha_reunion, observaciones, fecha_creacion, " +
                     "pdf_nombre, pdf_contenido, pdf_tipo_mime) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, acta.getComision().getId());
            stmt.setDate(2, new java.sql.Date(acta.getFechaReunion().getTime()));
            stmt.setString(3, acta.getObservaciones());
            stmt.setTimestamp(4, new Timestamp(acta.getFechaCreacion().getTime()));
            stmt.setString(5, acta.getPdfNombre());
            stmt.setBytes(6, acta.getPdfContenido());
            stmt.setString(7, acta.getPdfTipoMime());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }
    
    /**
     * Guarda una asistencia usando una conexión existente
     */
    private Long saveAsistencia(Connection conn, Long actaId, Long miembroId, 
                               boolean asistio, String justificacion) throws SQLException {
        String sql = "INSERT INTO asistencias_actas (acta_id, miembro_id, asistio, justificacion, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, actaId);
            stmt.setLong(2, miembroId);
            stmt.setBoolean(3, asistio);
            stmt.setString(4, justificacion);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }
    
    /**
     * Guarda el acta con todas sus asistencias en una transacción atómica
     */
    public Long saveActaConAsistencias(Acta acta, Map<Long, Boolean> asistencias, 
                                       Map<Long, String> justificaciones) throws SQLException {
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Guardar acta
            Long actaId = saveActa(conn, acta);
            
            if (actaId == null) {
                throw new SQLException("No se pudo generar ID para el acta");
            }
            
            AppLogger.debug("Acta guardada con ID: " + actaId);
            
            // 2. Guardar asistencias
            int guardadas = 0;
            for (Map.Entry<Long, Boolean> entry : asistencias.entrySet()) {
                Long miembroId = entry.getKey();
                Boolean asistio = entry.getValue();
                String justificacion = justificaciones.get(miembroId);
                
                saveAsistencia(conn, actaId, miembroId, asistio, justificacion);
                guardadas++;
            }
            
            AppLogger.debug("Asistencias guardadas: " + guardadas);
            
            conn.commit();
            AppLogger.info("Acta y asistencias guardadas correctamente");
            
            return actaId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    AppLogger.error("Transacción revertida debido a error", e);
                } catch (SQLException ex) {
                    AppLogger.error("Error al hacer rollback", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    AppLogger.error("Error al cerrar conexión", e);
                }
            }
        }
    }
    
    /**
     * Guarda una asistencia de acta
     */
    public Long saveAsistencia(Long actaId, Long miembroId, boolean asistio, String justificacion) 
            throws SQLException {
        
        String sql = "INSERT INTO asistencias_actas (acta_id, miembro_id, asistio, justificacion, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        AppLogger.debug("Guardando asistencia para miembro ID: " + miembroId);
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, actaId);
            stmt.setLong(2, miembroId);
            stmt.setBoolean(3, asistio);
            stmt.setString(4, justificacion);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("No se pudo guardar la asistencia");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);
                    AppLogger.debug("Asistencia guardada con ID: " + generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Error al guardar asistencia", e);
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
                     "FROM asistencias_actas aa " +
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
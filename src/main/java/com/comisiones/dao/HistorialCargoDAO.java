package com.comisiones.dao;

import com.comisiones.model.HistorialCargo;
import com.comisiones.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar el historial de cambios de cargo de miembros en comisiones.
 */
public class HistorialCargoDAO {
    
    private static final String TABLE_NAME = "comision_miembro_historial_cargos";
    
    /**
     * Obtiene el historial completo de cambios de cargo de un miembro en una comisión.
     * Usa la clave compuesta (comision_id, miembro_id) para identificar al miembro.
     * 
     * @param comisionId ID de la comisión
     * @param miembroId ID del miembro
     * @return Lista de cambios ordenados por fecha (más reciente primero)
     */
    public List<HistorialCargo> getHistorialByComisionMiembro(Long comisionId, Long miembroId) throws SQLException {
        List<HistorialCargo> historial = new ArrayList<>();
        
        String sql = "SELECT id, comision_id, miembro_id, cargo_anterior, cargo_nuevo, " +
                     "fecha_cambio, motivo, usuario_modificacion, created_at, created_by " +
                     "FROM " + TABLE_NAME + " " +
                     "WHERE comision_id = ? AND miembro_id = ? " +
                     "ORDER BY fecha_cambio DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, comisionId);
            stmt.setLong(2, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historial.add(extractHistorialCargoFromResultSet(rs));
                }
            }
        }
        
        return historial;
    }
    
    /**
     * Obtiene el último cambio de cargo de un miembro en una comisión.
     * 
     * @param comisionId ID de la comisión
     * @param miembroId ID del miembro
     * @return El último cambio registrado, o null si no hay cambios
     */
    public HistorialCargo getUltimoCambio(Long comisionId, Long miembroId) throws SQLException {
        String sql = "SELECT id, comision_id, miembro_id, cargo_anterior, cargo_nuevo, " +
                     "fecha_cambio, motivo, usuario_modificacion, created_at, created_by " +
                     "FROM " + TABLE_NAME + " " +
                     "WHERE comision_id = ? AND miembro_id = ? " +
                     "ORDER BY fecha_cambio DESC " +
                     "LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, comisionId);
            stmt.setLong(2, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractHistorialCargoFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los cambios de cargo realizados en una comisión.
     * 
     * @param comisionId ID de la comisión
     * @return Lista de todos los cambios en la comisión
     */
    public List<HistorialCargo> getHistorialByComision(Long comisionId) throws SQLException {
        List<HistorialCargo> historial = new ArrayList<>();
        
        String sql = "SELECT h.id, h.comision_id, h.miembro_id, h.cargo_anterior, h.cargo_nuevo, " +
                     "h.fecha_cambio, h.motivo, h.usuario_modificacion, h.created_at, h.created_by, " +
                     "m.nombre_apellidos " +
                     "FROM " + TABLE_NAME + " h " +
                     "INNER JOIN miembros m ON h.miembro_id = m.id " +
                     "WHERE h.comision_id = ? " +
                     "ORDER BY h.fecha_cambio DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, comisionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistorialCargo hc = extractHistorialCargoFromResultSet(rs);
                    try {
                        hc.setNombreMiembro(rs.getString("nombre_apellidos"));
                    } catch (SQLException e) {
                        // Campo opcional
                    }
                    historial.add(hc);
                }
            }
        }
        
        return historial;
    }
    
    /**
     * Obtiene todos los cambios de cargo de un miembro en todas sus comisiones.
     * 
     * @param miembroId ID del miembro
     * @return Lista de todos los cambios del miembro
     */
    public List<HistorialCargo> getHistorialByMiembro(Long miembroId) throws SQLException {
        List<HistorialCargo> historial = new ArrayList<>();
        
        String sql = "SELECT h.id, h.comision_id, h.miembro_id, h.cargo_anterior, h.cargo_nuevo, " +
                     "h.fecha_cambio, h.motivo, h.usuario_modificacion, h.created_at, h.created_by, " +
                     "c.nombre as nombre_comision " +
                     "FROM " + TABLE_NAME + " h " +
                     "INNER JOIN comisiones c ON h.comision_id = c.id " +
                     "WHERE h.miembro_id = ? " +
                     "ORDER BY h.fecha_cambio DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistorialCargo hc = extractHistorialCargoFromResultSet(rs);
                    try {
                        hc.setNombreComision(rs.getString("nombre_comision"));
                    } catch (SQLException e) {
                        // Campo opcional
                    }
                    historial.add(hc);
                }
            }
        }
        
        return historial;
    }
    
    /**
     * Actualiza el motivo del último cambio de cargo.
     * Útil para agregar motivo después de que el trigger haya registrado el cambio.
     * 
     * @param comisionId ID de la comisión
     * @param miembroId ID del miembro
     * @param motivo Motivo del cambio
     */
    public void actualizarMotivoUltimoCambio(Long comisionId, Long miembroId, String motivo) throws SQLException {
        // PostgreSQL no permite ORDER BY en UPDATE directamente, así que usamos un subquery
        String sql = "UPDATE " + TABLE_NAME + " " +
                     "SET motivo = ? " +
                     "WHERE id = (" +
                     "  SELECT id FROM " + TABLE_NAME + " " +
                     "  WHERE comision_id = ? AND miembro_id = ? " +
                     "  ORDER BY fecha_cambio DESC " +
                     "  LIMIT 1" +
                     ")";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, motivo);
            stmt.setLong(2, comisionId);
            stmt.setLong(3, miembroId);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Extrae un objeto HistorialCargo desde un ResultSet.
     * 
     * @param rs ResultSet posicionado en el registro a extraer
     * @return Objeto HistorialCargo con los datos del registro
     */
    private HistorialCargo extractHistorialCargoFromResultSet(ResultSet rs) throws SQLException {
        HistorialCargo hc = new HistorialCargo();
        
        hc.setId(rs.getLong("id"));
        hc.setComisionId(rs.getLong("comision_id"));
        hc.setMiembroId(rs.getLong("miembro_id"));
        hc.setCargoAnterior(rs.getString("cargo_anterior"));
        hc.setCargoNuevo(rs.getString("cargo_nuevo"));
        hc.setFechaCambio(rs.getTimestamp("fecha_cambio"));
        hc.setMotivo(rs.getString("motivo"));
        hc.setUsuarioModificacion(rs.getString("usuario_modificacion"));
        hc.setCreatedAt(rs.getTimestamp("created_at"));
        hc.setCreatedBy(rs.getString("created_by"));
        
        return hc;
    }
}

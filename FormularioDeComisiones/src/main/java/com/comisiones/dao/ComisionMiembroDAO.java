package com.comisiones.dao;

import com.comisiones.model.ComisionMiembro;
import com.comisiones.model. Comision;
import com.comisiones.model.Miembro;
import com.comisiones.util.DBUtil;
import java. sql.*;
import java.util. ArrayList;
import java.util. List;

public class ComisionMiembroDAO {
    
    private static final String TABLE_NAME = "comision_miembros";
    
    public void save(ComisionMiembro comisionMiembro) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (comision_id, miembro_id, cargo, fecha_incorporacion) VALUES (?, ?, ? :: cargo_type, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt. setLong(1, comisionMiembro.getComision().getId());
            stmt.setLong(2, comisionMiembro.getMiembro().getId());
            stmt.setString(3, comisionMiembro.getCargo().name());
            stmt.setDate(4, new java.sql.Date(comisionMiembro.getFechaIncorporacion().getTime()));
            
            stmt.executeUpdate();
            
            // ⭐ NO hay RETURN_GENERATED_KEYS porque no hay columna id autogenerada
            // La tabla usa clave primaria compuesta (comision_id, miembro_id)
        }
    }
    
    public List<ComisionMiembro> findByComisionId(Long comisionId) throws SQLException {
        List<ComisionMiembro> lista = new ArrayList<>();
        // ⭐ SIN cm.id - solo seleccionar las columnas que existen
        String sql = "SELECT cm.cargo, cm.fecha_incorporacion, cm.fecha_baja, " +
                     "cm.comision_id, cm.miembro_id, " +
                     "m.dni_nif, m.nombre_apellidos, m.correo_electronico, " +
                     "c.nombre as comision_nombre " +
                     "FROM " + TABLE_NAME + " cm " +
                     "INNER JOIN miembros m ON cm.miembro_id = m. id " +
                     "INNER JOIN comisiones c ON cm.comision_id = c.id " +
                     "WHERE cm.comision_id = ? " +
                     "ORDER BY cm.cargo, m.nombre_apellidos";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, comisionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extractComisionMiembroFromResultSet(rs));
                }
            }
        }
        return lista;
    }
    
    public List<ComisionMiembro> findByMiembroId(Long miembroId) throws SQLException {
        List<ComisionMiembro> lista = new ArrayList<>();
        // ⭐ SIN cm.id
        String sql = "SELECT cm. cargo, cm.fecha_incorporacion, cm.fecha_baja, " +
                     "cm. comision_id, cm.miembro_id, " +
                     "m.dni_nif, m.nombre_apellidos, m.correo_electronico, " +
                     "c. nombre as comision_nombre, c.area, c.tipo, c.fecha_constitucion, c.fecha_fin " +
                     "FROM " + TABLE_NAME + " cm " +
                     "INNER JOIN miembros m ON cm.miembro_id = m.id " +
                     "INNER JOIN comisiones c ON cm.comision_id = c.id " +
                     "WHERE cm.miembro_id = ? " +
                     "ORDER BY c.nombre";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn. prepareStatement(sql)) {
            
            stmt.setLong(1, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extractComisionMiembroFromResultSet(rs));
                }
            }
        }
        return lista;
    }
    
    public void darDeBaja(Long comisionId, Long miembroId, java.sql.Date fechaBaja) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET fecha_baja = ? WHERE comision_id = ? AND miembro_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, fechaBaja);
            stmt.setLong(2, comisionId);
            stmt.setLong(3, miembroId);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("[darDeBaja] Filas afectadas: " + rowsAffected);
        }
    }
    
    /**
     * Verifica si un miembro ya está asignado a una comisión específica (activo)
     */
    public boolean existeEnComision(Long comisionId, Long miembroId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE comision_id = ? AND miembro_id = ?  AND fecha_baja IS NULL";
        
        try (Connection conn = DBUtil. getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, comisionId);
            stmt.setLong(2, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs. next()) {
                    int count = rs.getInt(1);
                    System.out.println("===> existeEnComision(comisionId=" + comisionId + ", miembroId=" + miembroId + ") = " + (count > 0));
                    return count > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Obtiene todas las comisiones a las que pertenece un miembro (actualmente activo)
     */
    public List<Long> getComisionesByMiembroId(Long miembroId) throws SQLException {
        List<Long> comisionesIds = new ArrayList<>();
        String sql = "SELECT comision_id FROM " + TABLE_NAME + " WHERE miembro_id = ?  AND fecha_baja IS NULL";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn. prepareStatement(sql)) {
            
            stmt.setLong(1, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comisionesIds.add(rs.getLong("comision_id"));
                }
            }
        }
        return comisionesIds;
    }
    
    /**
     * Cuenta cuántas comisiones activas tiene un miembro
     */
    public int contarComisionesActivas(Long miembroId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE miembro_id = ? AND fecha_baja IS NULL";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, miembroId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    private ComisionMiembro extractComisionMiembroFromResultSet(ResultSet rs) throws SQLException {
        ComisionMiembro cm = new ComisionMiembro();
        
        // ⭐ NO setear ID - la tabla no tiene columna id
        // La relación se identifica únicamente por (comision_id, miembro_id)
        
        // Miembro
        Miembro miembro = new Miembro();
        miembro.setId(rs.getLong("miembro_id"));
        miembro.setDniNif(rs.getString("dni_nif"));
        miembro.setNombreApellidos(rs.getString("nombre_apellidos"));
        
        // Correo electrónico
        try {
            String correo = rs.getString("correo_electronico");
            miembro.setEmail(correo);
        } catch (SQLException e) {
            miembro.setEmail(null);
        }
        
        cm.setMiembro(miembro);
        
        // Comisión
        Comision comision = new Comision();
        comision.setId(rs.getLong("comision_id"));
        comision.setNombre(rs.getString("comision_nombre"));
        
        // Área (opcional, solo en algunas queries)
        try {
            String areaStr = rs.getString("area");
            if (areaStr != null) {
                comision.setArea(Comision.Area.valueOf(areaStr));
            }
        } catch (SQLException e) {
            // Campo no disponible en esta query
        }
        
        // Tipo (opcional, solo en algunas queries)
        try {
            String tipoStr = rs.getString("tipo");
            if (tipoStr != null) {
                comision.setTipo(Comision.Tipo.valueOf(tipoStr));
            }
        } catch (SQLException e) {
            // Campo no disponible en esta query
        }
        
        // Fecha constitución (opcional)
        try {
            Date fechaConst = rs.getDate("fecha_constitucion");
            if (fechaConst != null) {
                comision.setFechaConstitucion(fechaConst);
            }
        } catch (SQLException e) {
            // Campo no disponible
        }
        
        // Fecha fin (opcional)
        try {
            Date fechaFin = rs.getDate("fecha_fin");
            if (fechaFin != null) {
                comision.setFechaFin(fechaFin);
            }
        } catch (SQLException e) {
            // Campo no disponible
        }
        
        cm.setComision(comision);
        
        // Cargo
        String cargoStr = rs.getString("cargo");
        if (cargoStr != null) {
            cm.setCargo(ComisionMiembro.Cargo. valueOf(cargoStr));
        }
        
        // Fechas de incorporación y baja
        cm.setFechaIncorporacion(rs.getDate("fecha_incorporacion"));
        cm.setFechaBaja(rs.getDate("fecha_baja"));
        
        return cm;
    }
}
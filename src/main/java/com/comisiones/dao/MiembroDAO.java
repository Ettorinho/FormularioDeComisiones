package com.comisiones.dao;

import com.comisiones.model.Miembro;
import com.comisiones.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MiembroDAO {
    
    public void save(Miembro miembro) throws SQLException {
        String sql = "INSERT INTO miembros (nombre_apellidos, dni_nif, correo_electronico) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, miembro.getNombreApellidos());
            stmt.setString(2, miembro.getDniNif());
            stmt.setString(3, miembro.getEmail());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    miembro.setId(generatedKeys.getLong(1));
                }
            }
        }
    }
    
    public Miembro findById(Long id) throws SQLException {
        String sql = "SELECT * FROM miembros WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMiembroFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public Miembro findByDni(String dniNif) throws SQLException {
        String sql = "SELECT * FROM miembros WHERE dni_nif = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dniNif);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMiembroFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public List<Miembro> findAll() throws SQLException {
        List<Miembro> miembros = new ArrayList<>();
        String sql = "SELECT * FROM miembros ORDER BY nombre_apellidos";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                miembros.add(extractMiembroFromResultSet(rs));
            }
        }
        return miembros;
    }
    
    /**
     * NUEVO MÉTODO: Busca todos los miembros de una comisión específica
     */
public List<Miembro> findMiembrosByComisionId(Long comisionId) throws SQLException {
    List<Miembro> miembros = new ArrayList<>();
    
    String sql = "SELECT m.id, m.nombre_apellidos, m.dni_nif, m.correo_electronico " +
                 "FROM miembros m " +
                 "INNER JOIN comision_miembros cm ON m.id = cm.miembro_id " +
                 "WHERE cm.comision_id = ? " +
                 "ORDER BY m.nombre_apellidos";
    
    System.out.println("\n=== DEBUG findMiembrosByComisionId ===");
    System.out.println("SQL: " + sql);
    System.out.println("Parámetro comisionId: " + comisionId);
    
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setLong(1, comisionId);
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // DEBUG: Ver los valores del ResultSet ANTES de crear el objeto
                Long id = rs.getLong("id");
                String nombreApellidos = rs.getString("nombre_apellidos");
                String dniNif = rs.getString("dni_nif");
                String email = rs.getString("correo_electronico");
                
                System.out.println("--- ResultSet valores ---");
                System.out.println("  id (Long): " + id);
                System.out.println("  nombre_apellidos: " + nombreApellidos);
                System.out.println("  dni_nif: " + dniNif);
                System.out.println("  correo_electronico: " + email);
                
                Miembro miembro = new Miembro();
                miembro.setId(id);
                miembro.setNombreApellidos(nombreApellidos);
                miembro.setDniNif(dniNif);
                miembro.setEmail(email);
                
                System.out.println("--- Miembro después de setters ---");
                System.out.println("  miembro.getId(): " + miembro.getId());
                System.out.println("  miembro.getNombreApellidos(): " + miembro.getNombreApellidos());
                System.out.println("  miembro.getDniNif(): " + miembro.getDniNif());
                System.out.println("  miembro object: " + miembro);
                
                miembros.add(miembro);
            }
        }
    }
    
    System.out.println("Total miembros añadidos a la lista: " + miembros.size());
    System.out.println("=== FIN DEBUG ===\n");
    
    return miembros;
}
    
    private Miembro extractMiembroFromResultSet(ResultSet rs) throws SQLException {
        Miembro miembro = new Miembro();
        miembro.setId(rs.getLong("id"));
        miembro.setNombreApellidos(rs.getString("nombre_apellidos"));
        miembro.setDniNif(rs.getString("dni_nif"));
        miembro.setEmail(rs.getString("correo_electronico"));
        return miembro;
    }
}
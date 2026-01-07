package com.comisiones.service;

import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Miembro;
import java.sql.SQLException;
import java.util.List;

public class MiembroService {
    
    private final MiembroDAO miembroDAO = new MiembroDAO();
    
    public void guardarMiembro(Miembro miembro) throws SQLException {
        miembroDAO.save(miembro);
    }
    
    public Miembro buscarMiembroPorId(Long id) throws SQLException {
        return miembroDAO.findById(id);
    }
    
    public Miembro buscarMiembroPorDni(String dni) throws SQLException {
        return miembroDAO.findByDni(dni);
    }
    
    public List<Miembro> getAllMiembros() throws SQLException {
        return miembroDAO.findAll();
    }
    
    public boolean existeDni(String dni) throws SQLException {
        return miembroDAO.findByDni(dni) != null;
    }
}
package com.comisiones.service;

import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ComisionService {
    
    private ComisionDAO comisionDAO = new ComisionDAO();
    private MiembroDAO miembroDAO = new MiembroDAO();
    private ComisionMiembroDAO comisionMiembroDAO = new ComisionMiembroDAO();
    
    public void createComision(Comision comision) throws Exception {
        if (comisionDAO.exists(comision.getNombre())) {
            throw new Exception("Ya existe una comisión con este nombre.");
        }
        comisionDAO.save(comision);
    }
    
    public List<Comision> getAllComisiones() throws SQLException {
        return comisionDAO.findAll();
    }
    
    public Comision getComisionById(Long id) throws SQLException {
        return comisionDAO.findById(id);
    }
    
    public void addMiembroToComision(Comision comision, Miembro miembro, ComisionMiembro.Cargo cargo, Date fechaIncorporacion) throws SQLException {
        Miembro existingMiembro = miembroDAO.findByDni(miembro.getDniNif());
        if (existingMiembro == null) {
            miembroDAO.save(miembro);
        } else {
            miembro.setId(existingMiembro.getId());
        }
        ComisionMiembro comisionMiembro = new ComisionMiembro(comision, miembro, cargo, fechaIncorporacion);
        comisionMiembroDAO.save(comisionMiembro);
    }
    
    public List<ComisionMiembro> getMiembrosByComision(Long comisionId) throws SQLException {
        return comisionMiembroDAO.findByComisionId(comisionId);
    }
}
package com.comisiones.service;

import com. comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com. comisiones.dao.MiembroDAO;
import com.comisiones.model.Comision;
import com.comisiones.model. Comision.Area;
import com.comisiones.model.Comision. Tipo;
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
        if (comision.getArea() == null || comision. getTipo() == null) {
            throw new Exception("La comisión debe tener área y tipo definidos");
        }
        
        if (comisionDAO.exists(comision.getNombre(), comision.getArea(), comision.getTipo())) {
            throw new Exception("Ya existe una " + comision.getTipo().getDescripcion() + 
                              " con este nombre en " + comision.getArea().getDescripcion());
        }
        comisionDAO.save(comision);
    }
    
    public List<Comision> getAllComisiones() throws SQLException {
        return comisionDAO. findAll();
    }
    
    public Comision getComisionById(Long id) throws SQLException {
        return comisionDAO.findById(id);
    }
    
    public List<Comision> getComisionesByAreaAndTipo(Area area, Tipo tipo) throws SQLException {
        return comisionDAO.findByAreaAndTipo(area, tipo);
    }
    
    /**
     * ⭐ ACTUALIZADO: Agrega un miembro a una comisión con validación de duplicados
     */
    public void addMiembroToComision(Comision comision, Miembro miembro, ComisionMiembro. Cargo cargo, Date fechaIncorporacion) throws Exception {
        // Buscar si el miembro ya existe
        Miembro existingMiembro = miembroDAO. findByDni(miembro. getDniNif());
        if (existingMiembro == null) {
            miembroDAO.save(miembro);
        } else {
            miembro.setId(existingMiembro.getId());
        }
        
        // ⭐ VALIDACIÓN:  Verificar si ya está en la comisión
        if (comisionMiembroDAO.existeEnComision(comision. getId(), miembro.getId())) {
            throw new Exception("El miembro " + miembro.getNombreApellidos() + " ya pertenece a esta comisión");
        }
        
        // Agregar a la comisión
        ComisionMiembro comisionMiembro = new ComisionMiembro(comision, miembro, cargo, fechaIncorporacion);
        comisionMiembroDAO. save(comisionMiembro);
    }
    
    public List<ComisionMiembro> getMiembrosByComision(Long comisionId) throws SQLException {
        return comisionMiembroDAO.findByComisionId(comisionId);
    }
    
    /**
     * ⭐ NUEVO: Obtiene el número de comisiones activas de un miembro
     */
    public int getNumeroComisionesActivas(Long miembroId) throws SQLException {
        return comisionMiembroDAO.contarComisionesActivas(miembroId);
    }
    
    /**
     * ⭐ NUEVO: Verifica si un miembro puede ser agregado a una comisión
     */
    public boolean puedeAgregarMiembro(Long comisionId, Long miembroId) throws SQLException {
        return ! comisionMiembroDAO. existeEnComision(comisionId, miembroId);
    }
}
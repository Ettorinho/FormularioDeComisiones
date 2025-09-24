package com.comisiones.model;

import java.io.Serializable;
import java.util.Date;

public class ComisionMiembro implements Serializable {

    public enum Cargo {
        REFERENTE, RESPONSABLE, PRESIDENTE, MIEMBRO, SECRETARIO
    }

    private Comision comision;
    private Miembro miembro;
    private Cargo cargo;
    private Date fechaIncorporacion;
    private Date fechaBaja;

    public ComisionMiembro() {}

    public ComisionMiembro(Comision comision, Miembro miembro, Cargo cargo, Date fechaIncorporacion) {
        this.comision = comision;
        this.miembro = miembro;
        this.cargo = cargo;
        this.fechaIncorporacion = fechaIncorporacion;
    }

    // Getters y setters
    public Comision getComision() { return comision; }
    public void setComision(Comision comision) { this.comision = comision; }
    public Miembro getMiembro() { return miembro; }
    public void setMiembro(Miembro miembro) { this.miembro = miembro; }
    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }
    public Date getFechaIncorporacion() { return fechaIncorporacion; }
    public void setFechaIncorporacion(Date fechaIncorporacion) { this.fechaIncorporacion = fechaIncorporacion; }
    public Date getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(Date fechaBaja) { this.fechaBaja = fechaBaja; }
}
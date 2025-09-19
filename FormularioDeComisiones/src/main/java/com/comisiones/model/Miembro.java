package com.comisiones.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Miembro implements Serializable {
    
    private Long id;
    private String nombreApellidos;
    private String dniNif;
    private String email;
    private Set<ComisionMiembro> comisiones = new HashSet<>();
    
    public Miembro() {}
    
    public Miembro(String nombreApellidos, String dniNif, String email) {
        this.nombreApellidos = nombreApellidos;
        this.dniNif = dniNif;
        this.email = email;
    }
    
    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreApellidos() { return nombreApellidos; }
    public void setNombreApellidos(String nombreApellidos) { this.nombreApellidos = nombreApellidos; }
    public String getDniNif() { return dniNif; }
    public void setDniNif(String dniNif) { this.dniNif = dniNif; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<ComisionMiembro> getComisiones() { return comisiones; }
    public void setComisiones(Set<ComisionMiembro> comisiones) { this.comisiones = comisiones; }
}
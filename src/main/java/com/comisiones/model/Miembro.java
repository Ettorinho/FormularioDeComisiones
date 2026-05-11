package com.comisiones.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Miembro implements Serializable {
    
    private Long id;

    @NotBlank(message = "El nombre y apellidos son obligatorios")
    @Size(min = 2, max = 200, message = "El nombre y apellidos deben tener entre 2 y 200 caracteres")
    private String nombreApellidos;

    @NotNull(message = "El DNI/NIE es obligatorio")
    @Pattern(regexp = "^[0-9]{8}[A-Z]$|^[XYZ][0-9]{7}[A-Z]$",
             message = "DNI/NIE inválido (formato: 12345678A o X1234567A)")
    private String dniNif;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
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
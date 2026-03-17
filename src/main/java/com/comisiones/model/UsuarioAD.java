package com.comisiones.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un usuario autenticado mediante Active Directory (AD).
 * Se almacena en la HttpSession bajo el atributo "usuarioLogueado".
 */
public class UsuarioAD implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String nombreCompleto;
    private String email;
    private String departamento;
    private String cargo;
    private List<String> roles;

    public UsuarioAD() {
        this.roles = new ArrayList<>();
    }

    public UsuarioAD(String username, String nombreCompleto, String email,
                     String departamento, String cargo, List<String> roles) {
        this.username       = username;
        this.nombreCompleto = nombreCompleto;
        this.email          = email;
        this.departamento   = departamento;
        this.cargo          = cargo;
        this.roles          = roles != null ? roles : new ArrayList<>();
    }

    public UsuarioAD(String username, String displayName, String email) {
        this(username, displayName, email, "", "", new ArrayList<>());
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDisplayName() { return nombreCompleto; }
    public void setDisplayName(String displayName) { this.nombreCompleto = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles != null ? roles : new ArrayList<>(); }

    public boolean tieneRol(String rol) {
        if (rol == null || roles == null) return false;
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(rol));
    }

    @Override
    public String toString() {
        return "UsuarioAD{username='" + username + "', nombreCompleto='" + nombreCompleto
                + "', roles=" + roles + "}";
    }
}

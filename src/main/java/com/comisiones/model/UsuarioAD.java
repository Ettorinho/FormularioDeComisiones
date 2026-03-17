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

    /** Constructor de compatibilidad (3 parámetros) */
    public UsuarioAD(String username, String nombreCompleto, String email) {
        this(username, nombreCompleto, email, "", "", new ArrayList<>());
    }

    // --- username ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // --- nombreCompleto / displayName (alias) ---
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    /** Alias para compatibilidad con codigo que use displayName */
    public String getDisplayName() { return nombreCompleto; }
    public void setDisplayName(String displayName) { this.nombreCompleto = displayName; }

    // --- email ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // --- departamento ---
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    // --- cargo ---
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    // --- roles ---
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles != null ? roles : new ArrayList<>(); }

    /**
     * Comprueba si el usuario tiene un rol determinado (case-insensitive).
     */
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
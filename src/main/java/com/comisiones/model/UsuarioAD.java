package com.comisiones.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo del usuario autenticado contra el Active Directory.
 * Se guarda en sesión HTTP tras un login exitoso.
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
        this.username      = username;
        this.nombreCompleto = nombreCompleto;
        this.email         = email;
        this.departamento  = departamento;
        this.cargo         = cargo;
        this.roles         = roles != null ? roles : new ArrayList<>();
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles != null ? roles : new ArrayList<>(); }

    // ── Métodos de utilidad ────────────────────────────────────────────────

    /**
     * Verifica si el usuario posee el rol indicado (comparación sin distinción de mayúsculas).
     *
     * @param rol nombre del rol a comprobar
     * @return {@code true} si el usuario tiene el rol
     */
    public boolean tieneRol(String rol) {
        if (rol == null || roles == null) return false;
        for (String r : roles) {
            if (rol.equalsIgnoreCase(r)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UsuarioAD{username='" + username + "', nombreCompleto='" + nombreCompleto
                + "', departamento='" + departamento + "', roles=" + roles + "}";
    }
}

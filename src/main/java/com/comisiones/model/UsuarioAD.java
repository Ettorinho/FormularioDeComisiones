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
    private String department;
    private String title;
    /** CNs de los grupos AD a los que pertenece el usuario (extraídos de memberOf). */
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
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.roles = new ArrayList<>();
    }

    public UsuarioAD(String username, String displayName, String email,
                     String department, String title, List<String> roles) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.department = department;
        this.title = title;
        this.roles = roles != null ? roles : new ArrayList<>();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDisplayName() { return nombreCompleto; }
    public void setDisplayName(String displayName) { this.nombreCompleto = displayName; }

    /** Alias de displayName, para compatibilidad con el nombre del AD. */
    public String getNombreCompleto() { return displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    @Override
    public String toString() {
        return "UsuarioAD{username='" + username + "', nombreCompleto='" + nombreCompleto
                + "', roles=" + roles + "}";
    }
}

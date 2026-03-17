package com.comisiones.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un usuario autenticado mediante Active Directory (AD).
 * Se almacena en la HttpSession bajo el atributo "usuarioLogueado".
 */
public class UsuarioAD implements Serializable {

    private String username;
    private String displayName;
    private String email;
    private String department;
    private String title;
    /** CNs de los grupos AD a los que pertenece el usuario (extraídos de memberOf). */
    private List<String> roles;

    public UsuarioAD() {}

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

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

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
        return "UsuarioAD{username='" + username + "', displayName='" + displayName + "'}";
    }
}

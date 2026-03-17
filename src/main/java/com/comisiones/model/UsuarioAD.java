package com.comisiones.model;

import java.io.Serializable;

/**
 * Representa un usuario autenticado mediante Active Directory (AD).
 * Se almacena en la HttpSession bajo el atributo "usuarioLogueado".
 */
public class UsuarioAD implements Serializable {

    private String username;
    private String displayName;
    private String email;

    public UsuarioAD() {}

    public UsuarioAD(String username, String displayName, String email) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "UsuarioAD{username='" + username + "', displayName='" + displayName + "'}";
    }
}

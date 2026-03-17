package com.comisiones.security;

/**
 * Constantes de roles internos de la aplicación.
 * Los nombres de los grupos AD correspondientes se configuran en context.xml via JNDI.
 */
public final class AppRoles {
    public static final String ADMIN   = "ADMIN";
    public static final String GESTOR  = "GESTOR";
    public static final String LECTURA = "LECTURA";

    private AppRoles() {}
}

package com.comisiones.security;

import com.comisiones.model.UsuarioAD;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Resuelve el rol interno de un usuario a partir de sus grupos AD.
 * Los nombres de los grupos AD se configuran en context.xml:
 *   roles/admin   → nombre del grupo AD para administradores
 *   roles/gestor  → nombre del grupo AD para gestores
 *   roles/lectura → nombre del grupo AD para usuarios de solo lectura
 *
 * Si algún parámetro JNDI no está definido, ese rol simplemente no se asigna.
 */
public class RolService {

    private final Map<String, String> grupoAdARol = new HashMap<>();

    public RolService() {
        try {
            Context envCtx = (Context) new InitialContext().lookup("java:comp/env");
            cargarRol(envCtx, "roles/admin",   AppRoles.ADMIN);
            cargarRol(envCtx, "roles/gestor",  AppRoles.GESTOR);
            cargarRol(envCtx, "roles/lectura", AppRoles.LECTURA);
        } catch (NamingException e) {
            // Si no hay configuración JNDI de roles, el sistema funcionará
            // pero ningún usuario tendrá rol asignado
        }
    }

    private void cargarRol(Context envCtx, String jndiKey, String rolInterno) {
        try {
            String grupoAd = (String) envCtx.lookup(jndiKey);
            if (grupoAd != null && !grupoAd.isBlank()) {
                grupoAdARol.put(grupoAd.trim(), rolInterno);
            }
        } catch (NamingException ignored) {
            // El parámetro JNDI no está configurado → ese rol no se asigna
        }
    }

    /**
     * Devuelve el rol interno del usuario (ADMIN, GESTOR o LECTURA),
     * o null si no tiene ningún grupo AD configurado.
     */
    public String resolverRol(UsuarioAD usuario) {
        if (usuario == null || usuario.getRoles() == null) return null;
        // Prioridad: ADMIN > GESTOR > LECTURA — buscamos el rol de mayor prioridad
        String mejorRol = null;
        for (String grupoAd : usuario.getRoles()) {
            String rol = grupoAdARol.get(grupoAd);
            if (AppRoles.ADMIN.equals(rol)) {
                return AppRoles.ADMIN; // Máxima prioridad, no hay que seguir
            }
            if (AppRoles.GESTOR.equals(rol) && !AppRoles.GESTOR.equals(mejorRol)) {
                mejorRol = AppRoles.GESTOR;
            } else if (AppRoles.LECTURA.equals(rol) && mejorRol == null) {
                mejorRol = AppRoles.LECTURA;
            }
        }
        return mejorRol;
    }

    /**
     * Devuelve true si el usuario tiene al menos el rol especificado
     * (ADMIN tiene todos los permisos, GESTOR tiene GESTOR y LECTURA, etc.)
     */
    public boolean tienePermiso(UsuarioAD usuario, String rolMinimo) {
        String rolUsuario = resolverRol(usuario);
        if (rolUsuario == null) return false;
        switch (rolMinimo) {
            case AppRoles.LECTURA: return true; // cualquier rol puede leer
            case AppRoles.GESTOR:  return AppRoles.GESTOR.equals(rolUsuario) || AppRoles.ADMIN.equals(rolUsuario);
            case AppRoles.ADMIN:   return AppRoles.ADMIN.equals(rolUsuario);
            default:               return false;
        }
    }
}

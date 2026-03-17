package com.comisiones.ldap;

import com.comisiones.model.UsuarioAD;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Servicio de autenticación contra Active Directory mediante bind LDAP.
 *
 * Flujo de dos pasos:
 * 1. Bind con las credenciales del usuario → verificar que la contraseña es correcta.
 * 2. Bind con la cuenta de servicio (bindDn/bindPassword) → leer memberOf con permisos suficientes.
 *
 * Si no se configura cuenta de servicio, se hace la búsqueda con el contexto del propio
 * usuario (comportamiento anterior, puede que memberOf no esté disponible).
 */
public class LdapAuthService {

    private final String ldapUrl;
    private final String baseDn;
    /** Dominio AD, p.ej. "empresa.com" */
    private final String dominioAd;
    /** DN de la cuenta de servicio para búsquedas (puede ser null) */
    private final String bindDn;
    /** Contraseña de la cuenta de servicio (puede ser null) */
    private final String bindPassword;

    /** Constructor sin cuenta de servicio (compatibilidad hacia atrás). */
    public LdapAuthService(String ldapUrl, String baseDn, String dominioAd) {
        this(ldapUrl, baseDn, dominioAd, null, null);
    }

    /** Constructor con cuenta de servicio para leer memberOf correctamente. */
    public LdapAuthService(String ldapUrl, String baseDn, String dominioAd,
                           String bindDn, String bindPassword) {
        this.ldapUrl      = ldapUrl;
        this.baseDn       = baseDn;
        this.dominioAd    = dominioAd;
        this.bindDn       = bindDn;
        this.bindPassword = bindPassword;
    }

    /**
     * Autentica al usuario contra el AD en dos pasos:
     * 1. Bind con las credenciales del usuario para verificar la contraseña.
     * 2. Bind con la cuenta de servicio para leer los atributos (incluido memberOf).
     *
     * @param username nombre de usuario (con o sin @dominio)
     * @param password contraseña del usuario
     * @return {@link UsuarioAD} con los datos del usuario autenticado
     * @throws AuthenticationException si las credenciales son incorrectas
     * @throws NamingException         si hay un error de conexión u otro error LDAP
     */
    public UsuarioAD autenticar(String username, String password) throws NamingException {
        // Construir el userPrincipalName para el bind
        String principal = username.contains("@") ? username : username + "@" + dominioAd;

        // PASO 1: Bind con las credenciales del usuario → solo para verificar la contraseña
        Hashtable<String, String> envUsuario = new Hashtable<>();
        envUsuario.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        envUsuario.put(Context.PROVIDER_URL, ldapUrl);
        envUsuario.put(Context.SECURITY_AUTHENTICATION, "simple");
        envUsuario.put(Context.SECURITY_PRINCIPAL, principal);
        envUsuario.put(Context.SECURITY_CREDENTIALS, password);
        // Timeouts de 5 segundos para evitar bloqueos prolongados
        envUsuario.put("com.sun.jndi.ldap.connect.timeout", "5000");
        envUsuario.put("com.sun.jndi.ldap.read.timeout",    "5000");

        DirContext ctxUsuario = null;
        try {
            // Lanza AuthenticationException si las credenciales son incorrectas
            ctxUsuario = new InitialDirContext(envUsuario);
        } finally {
            if (ctxUsuario != null) {
                try { ctxUsuario.close(); } catch (NamingException ignored) {}
            }
        }

        // PASO 2: Abrir contexto de búsqueda con la cuenta de servicio (si está configurada)
        // para poder leer memberOf con los permisos adecuados.
        DirContext ctxBusqueda = null;
        try {
            if (bindDn != null && !bindDn.trim().isEmpty()
                    && bindPassword != null && !bindPassword.trim().isEmpty()) {
                // Usar cuenta de servicio
                Hashtable<String, String> envServicio = new Hashtable<>();
                envServicio.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                envServicio.put(Context.PROVIDER_URL, ldapUrl);
                envServicio.put(Context.SECURITY_AUTHENTICATION, "simple");
                envServicio.put(Context.SECURITY_PRINCIPAL, bindDn);
                envServicio.put(Context.SECURITY_CREDENTIALS, bindPassword);
                envServicio.put("com.sun.jndi.ldap.connect.timeout", "5000");
                envServicio.put("com.sun.jndi.ldap.read.timeout",    "5000");
                ctxBusqueda = new InitialDirContext(envServicio);
            } else {
                // Fallback: reabrir con las credenciales del usuario
                ctxBusqueda = new InitialDirContext(envUsuario);
            }
            return buscarDatosUsuario(ctxBusqueda, username);
        } finally {
            if (ctxBusqueda != null) {
                try {
                    ctxBusqueda.close();
                } catch (NamingException e) {
                    // Se registra pero no se propaga para no ocultar una excepción original
                    System.err.println("[LdapAuthService] Advertencia: error al cerrar el contexto LDAP: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Busca los atributos del usuario en el directorio tras autenticar correctamente.
     * Busca primero por {@code userPrincipalName} si el username contiene '@',
     * o por {@code sAMAccountName} en caso contrario.
     * Si no encuentra el usuario (caso inusual), devuelve un {@link UsuarioAD} básico.
     */
    private UsuarioAD buscarDatosUsuario(DirContext ctx, String username) throws NamingException {
        // Determinar atributo de búsqueda según el formato del username
        String attrBusqueda;
        String valorBusqueda;
        if (username.contains("@")) {
            attrBusqueda  = "userPrincipalName";
            valorBusqueda = username;
        } else {
            attrBusqueda  = "sAMAccountName";
            valorBusqueda = username;
        }

        String filter = "(&(objectClass=user)(objectCategory=person)("
                + attrBusqueda + "=" + escapeLDAP(valorBusqueda) + "))";

        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(new String[]{
                "sAMAccountName", "displayName", "mail",
                "department", "title", "memberOf"
        });

        NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, sc);

        if (results != null && results.hasMore()) {
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();

            // Usar sAMAccountName como username canónico si está disponible
            String samAccount = getAttr(attrs, "sAMAccountName", username);

            return new UsuarioAD(
                    samAccount,
                    getAttr(attrs, "displayName", samAccount),
                    getAttr(attrs, "mail",        ""),
                    getAttr(attrs, "department",  ""),
                    getAttr(attrs, "title",       ""),
                    extraerRoles(attrs)
            );
        }

        // Si no se encontró el usuario tras autenticar (poco común), devolver objeto básico
        // Extraer la parte local si el username tiene formato usuario@dominio
        String usernameBasico = username.contains("@")
                ? username.substring(0, username.indexOf('@'))
                : username;
        return new UsuarioAD(usernameBasico, usernameBasico, "", "", "", new ArrayList<>());
    }

    /**
     * Extrae los roles del atributo {@code memberOf} del usuario.
     * Cada valor de memberOf es un DN de grupo AD; se extrae el CN de cada uno.
     */
    private List<String> extraerRoles(Attributes attrs) throws NamingException {
        List<String> roles = new ArrayList<>();
        if (attrs == null) return roles;

        Attribute memberOf = attrs.get("memberOf");
        if (memberOf == null) return roles;

        NamingEnumeration<?> valores = memberOf.getAll();
        while (valores.hasMore()) {
            Object val = valores.next();
            if (val != null) {
                String cn = extraerCN(val.toString());
                if (cn != null && !cn.isEmpty()) {
                    roles.add(cn);
                }
            }
        }
        return roles;
    }

    /**
     * Extrae el valor del CN de un Distinguished Name.
     * Ejemplo: "CN=GrupoAdmins,OU=Grupos,DC=empresa,DC=com" → "GrupoAdmins"
     */
    private String extraerCN(String dn) {
        if (dn == null || dn.isEmpty()) return "";
        for (String parte : dn.split(",")) {
            String parteTrim = parte.trim();
            if (parteTrim.toUpperCase().startsWith("CN=")) {
                return parteTrim.substring(3);
            }
        }
        return "";
    }

    /** Obtiene el valor de un atributo LDAP como String, o el valor por defecto si no existe. */
    private String getAttr(Attributes attrs, String name, String defaultValue) throws NamingException {
        if (attrs == null) return defaultValue;
        Attribute a = attrs.get(name);
        if (a == null) return defaultValue;
        Object v = a.get();
        return v != null ? v.toString().trim() : defaultValue;
    }

    /**
     * Escapa los caracteres especiales de un valor en un filtro de búsqueda LDAP
     * según RFC 4515.
     */
    private String escapeLDAP(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': sb.append("\\5c"); break;
                case '*':  sb.append("\\2a"); break;
                case '(':  sb.append("\\28"); break;
                case ')':  sb.append("\\29"); break;
                case '\0': sb.append("\\00"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
}

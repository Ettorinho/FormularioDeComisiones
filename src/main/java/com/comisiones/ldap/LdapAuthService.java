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
 * Utiliza las credenciales del propio usuario (no la cuenta de servicio)
 * para realizar el bind, y luego busca sus atributos y grupos.
 */
public class LdapAuthService {

    private final String ldapUrl;
    private final String baseDn;
    /** Dominio AD, p.ej. "empresa.com" */
    private final String dominioAd;

    public LdapAuthService(String ldapUrl, String baseDn, String dominioAd) {
        this.ldapUrl   = ldapUrl;
        this.baseDn    = baseDn;
        this.dominioAd = dominioAd;
    }

    /**
     * Autentica al usuario contra el AD realizando un bind LDAP con sus propias credenciales.
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

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, password);
        // Timeouts de 5 segundos para evitar bloqueos prolongados
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        env.put("com.sun.jndi.ldap.read.timeout",    "5000");

        DirContext ctx = null;
        try {
            // El bind falla con AuthenticationException si las credenciales son incorrectas
            ctx = new InitialDirContext(env);
            return buscarDatosUsuario(ctx, username);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
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

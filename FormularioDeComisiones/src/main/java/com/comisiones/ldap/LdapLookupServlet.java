package com.comisiones.ldap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;

/**
 * Servlet para lookup LDAP. La contraseña de bind se obtiene de:
 * 1) JNDI env: java:comp/env/ldap/bindPassword (recomendado, configurar en Tomcat)
 * 2) System.getenv("LDAP_BIND_PASSWORD") (fallback útil en desarrollo)
 * 3) Si no existe, no se usa bind (anonymous) o se puede fallar según tu política.
 */
public class LdapLookupServlet extends HttpServlet {

    private String ldapUrl;
    private String baseDn;
    private String bindDn;
    private String bindPassword;
    private String dniAttr; // nombre del atributo que contiene el DNI en tu directorio (ajustar)

    @Override
    public void init() throws ServletException {
        // Leer configuración desde context-params en web.xml
        ldapUrl = getServletContext().getInitParameter("ldap.url");
        baseDn = getServletContext().getInitParameter("ldap.baseDn");
        bindDn = getServletContext().getInitParameter("ldap.bindDn");
        dniAttr = getServletContext().getInitParameter("ldap.dniAttr");
        if (dniAttr == null || dniAttr.isEmpty()) {
            dniAttr = "uid";
        }

        // Intentar leer la contraseña desde JNDI (recomendado: configurar en Tomcat como <Environment>)
        try {
            InitialContext ic = new InitialContext();
            Object pw = ic.lookup("java:comp/env/ldap/bindPassword");
            if (pw != null) {
                bindPassword = pw.toString();
                System.out.println("LdapLookupServlet: bindPassword obtenido de java:comp/env/ldap/bindPassword");
            }
        } catch (Exception e) {
            // No encontrado en JNDI → fallback
            bindPassword = null;
        }

        // Fallback: variable de entorno (útil en desarrollo / CI)
        if ((bindPassword == null || bindPassword.isEmpty())) {
            String envPw = System.getenv("LDAP_BIND_PASSWORD");
            if (envPw != null && !envPw.isEmpty()) {
                bindPassword = envPw;
                System.out.println("LdapLookupServlet: bindPassword obtenido de variable de entorno LDAP_BIND_PASSWORD");
            }
        }

        // Nota: si bindPassword sigue null, se intentará conexión anonymous
        if (bindPassword == null) {
            System.out.println("LdapLookupServlet: bindPassword no configurado; se intentará conexión anónima si el servidor lo permite.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String dni = request.getParameter("dni");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (dni == null || dni.trim().isEmpty()) {
            out.write("{\"error\":\"Parámetro 'dni' requerido\"}");
            return;
        }

        try {
            Map<String, String> attrs = lookupByDni(dni.trim());
            if (attrs == null) {
                out.write("{\"found\":false}");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"found\":true");
                for (Map.Entry<String,String> e : attrs.entrySet()) {
                    sb.append(",");
                    sb.append("\"").append(escapeJson(e.getKey())).append("\":");
                    sb.append("\"").append(escapeJson(e.getValue())).append("\"");
                }
                sb.append("}");
                out.write(sb.toString());
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            out.write("{\"error\":\"Error en consulta LDAP\"}");
        }
    }

    private Map<String,String> lookupByDni(String dni) throws NamingException {
        // Conectar al servidor LDAP
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);

        if (bindDn != null && !bindDn.isEmpty() && bindPassword != null && !bindPassword.isEmpty()) {
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, bindDn);
            env.put(Context.SECURITY_CREDENTIALS, bindPassword);
        } else {
            env.put(Context.SECURITY_AUTHENTICATION, "none");
        }

        DirContext ctx = new InitialDirContext(env);

        try {
            String filter = "(& (objectClass=person) (" + dniAttr + "=" + escapeLDAPSearchFilter(dni) + ") )";
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setReturningAttributes(new String[] { "cn", "givenName", "sn", "mail", "telephoneNumber", "employeeNumber", "uid" });

            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, sc);
            if (results.hasMore()) {
                SearchResult sr = results.next();
                Attributes attributes = sr.getAttributes();
                Map<String,String> map = new HashMap<>();
                map.put("nombreApellidos", getAttr(attributes, "cn", (getAttr(attributes, "givenName", "") + " " + getAttr(attributes, "sn", "")).trim()));
                map.put("email", getAttr(attributes, "mail", ""));
                map.put("telefono", getAttr(attributes, "telephoneNumber", ""));
                map.put("uid", getAttr(attributes, "uid", ""));
                map.put("employeeNumber", getAttr(attributes, "employeeNumber", ""));
                return map;
            } else {
                return null;
            }
        } finally {
            try { ctx.close(); } catch(Exception ignored) {}
        }
    }

    private String getAttr(Attributes attrs, String name, String defaultValue) throws NamingException {
        if (attrs == null) return defaultValue;
        Attribute a = attrs.get(name);
        if (a == null) return defaultValue;
        Object v = a.get();
        return v != null ? v.toString() : defaultValue;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String escapeLDAPSearchFilter(String filter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filter.length(); i++) {
            char curChar = filter.charAt(i);
            switch (curChar) {
                case '\\': sb.append("\\5c"); break;
                case '*': sb.append("\\2a"); break;
                case '(': sb.append("\\28"); break;
                case ')': sb.append("\\29"); break;
                case '\0': sb.append("\\00"); break;
                default: sb.append(curChar);
            }
        }
        return sb.toString();
    }
}
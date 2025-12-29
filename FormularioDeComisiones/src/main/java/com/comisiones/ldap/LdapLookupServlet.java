package com.comisiones.ldap;

import javax.servlet.ServletException;
import javax. servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet. http.HttpServletResponse;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.naming.Context;
import javax.naming.NamingException;
import javax. naming.InitialContext;
import java.io.IOException;
import java.io.PrintWriter;
import java. util. Hashtable;
import java.util.Map;
import java.util.HashMap;

public class LdapLookupServlet extends HttpServlet {

    private String ldapUrl;
    private String baseDn;
    private String bindDn;
    private String bindPassword;
    private String dniAttr;

    @Override
    public void init() throws ServletException {
        log("========================================");
        log("Inicializando LdapLookupServlet...");
        log("========================================");
        
        // Leer configuración desde web.xml
        ldapUrl = getServletContext().getInitParameter("ldap.url");
        baseDn = getServletContext().getInitParameter("ldap.baseDn");
        bindDn = getServletContext(). getInitParameter("ldap.bindDn");
        dniAttr = getServletContext().getInitParameter("ldap.dniAttr");
        
        log("📋 Parámetros de web.xml:");
        log("   ldap.url = " + (ldapUrl != null ? ldapUrl : "❌ NULL"));
        log("   ldap.baseDn = " + (baseDn != null ?  baseDn : "❌ NULL"));
        log("   ldap.bindDn = " + (bindDn != null ? bindDn : "❌ NULL"));
        log("   ldap.dniAttr = " + (dniAttr != null ?  dniAttr : "❌ NULL"));
        
        // Leer password desde VM Options (-DLDAP_BIND_PASSWORD)
        log("🔐 Intentando leer contraseña.. .");
        bindPassword = System. getProperty("LDAP_BIND_PASSWORD");
        
        if (bindPassword != null && ! bindPassword.isEmpty()) {
            log("✅ Contraseña cargada desde VM Options (-DLDAP_BIND_PASSWORD)");
            log("   Longitud: " + bindPassword.length() + " caracteres");
        } else {
            log("⚠️ No se encontró en VM Options, intentando web.xml.. .");
            // Fallback: intentar leer desde web.xml
            bindPassword = getServletContext().getInitParameter("ldap.bindPassword");
            if (bindPassword != null && ! bindPassword.isEmpty()) {
                log("⚠️ Contraseña cargada desde web.xml (NO RECOMENDADO para producción)");
            } else {
                log("⚠️ No se encontró en web.xml, intentando context. xml...");
                // Último fallback: intentar leer desde context.xml
                try {
                    InitialContext initCtx = new InitialContext();
                    try {
                        bindPassword = (String) initCtx.lookup("java:comp/env/ldap/bindPassword");
                        log("✅ Contraseña cargada desde context. xml");
                    } catch (Exception e1) {
                        try {
                            bindPassword = (String) initCtx.lookup("ldap/bindPassword");
                            log("✅ Contraseña cargada desde context.xml (ruta alternativa)");
                        } catch (Exception e2) {
                            log("❌ No se encontró contraseña en context.xml");
                        }
                    }
                } catch (Exception e) {
                    log("❌ Error al buscar en context.xml: " + e. getMessage());
                }
            }
        }
        
        // Validar configuración
        if (dniAttr == null || dniAttr.isEmpty()) {
            dniAttr = "employeeNumber";
            log("⚠️ dniAttr no configurado, usando valor por defecto: employeeNumber");
        }
        
        StringBuilder errores = new StringBuilder();
        if (ldapUrl == null || ldapUrl.isEmpty()) {
            errores.append("- ldap.url no configurado en web.xml\n");
        }
        if (baseDn == null || baseDn.isEmpty()) {
            errores.append("- ldap.baseDn no configurado en web.xml\n");
        }
        if (bindDn == null || bindDn.isEmpty()) {
            errores.append("- ldap.bindDn no configurado en web.xml\n");
        }
        if (bindPassword == null || bindPassword.isEmpty()) {
            errores.append("- ldap.bindPassword no configurado (VM Options, web.xml o context.xml)\n");
        }
        
        if (errores.length() > 0) {
            log("❌ ERRORES DE CONFIGURACIÓN:");
            log(errores.toString());
            log("\n📝 SOLUCIONES:");
            log("1.  Para VM Options: Agrega en Tools → Servers → Platform → VM Options:");
            log("   -DLDAP_BIND_PASSWORD=TuPassword");
            log("2. Para web.xml: Agrega <context-param> con ldap.bindPassword");
            log("3. Para context.xml: Agrega <Environment name=\"ldap/bindPassword\" . ../>");
            throw new ServletException("Configuración LDAP incompleta:\n" + errores.toString());
        }
        
        log("========================================");
        log("✅ LDAP configurado correctamente");
        log("   URL: " + ldapUrl);
        log("   Base DN: " + baseDn);
        log("   Bind DN: " + bindDn);
        log("   DNI Attr: " + dniAttr);
        log("========================================");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String dni = request.getParameter("dni");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (dni == null || dni.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Parámetro 'dni' requerido\"}");
            return;
        }

        // Normalizar DNI (convertir a mayúsculas y quitar espacios)
        dni = dni.trim().toUpperCase();
        
        log("🔍 Búsqueda LDAP solicitada para DNI: " + dni);

        try {
            Map<String, String> attrs = lookupByDni(dni);
            if (attrs == null) {
                out.write("{\"found\":false,\"message\":\"Usuario no encontrado en LDAP\"}");
                log("⚠️ Usuario no encontrado: " + dni);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"found\":true");
                for (Map.Entry<String,String> e : attrs.entrySet()) {
                    sb.append(",");
                    sb.append("\""). append(escapeJson(e.getKey())).append("\":");
                    sb.append("\"").append(escapeJson(e.getValue())).append("\"");
                }
                sb. append("}");
                out. write(sb.toString());
                log("✅ Búsqueda exitosa para DNI: " + dni + " → " + attrs.get("nombreApellidos"));
            }
        } catch (NamingException ne) {
            log("❌ Error LDAP para DNI: " + dni);
            log("   Tipo: " + ne.getClass().getName());
            log("   Mensaje: " + ne.getMessage());
            if (ne.getRootCause() != null) {
                log("   Causa raíz: " + ne.getRootCause().getMessage());
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Error en consulta LDAP: " + escapeJson(ne.getMessage()) + "\"}");
        } catch (Exception e) {
            log("❌ Error inesperado para DNI: " + dni);
            log("   Tipo: " + e.getClass().getName());
            log("   Mensaje: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Error inesperado: " + escapeJson(e. getMessage()) + "\"}");
        }
    }

    private Map<String,String> lookupByDni(String dni) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        
        // Configuración de autenticación
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindDn);
        env.put(Context.SECURITY_CREDENTIALS, bindPassword);
        
        // Timeouts
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        env.put("com.sun.jndi.ldap.read.timeout", "5000");
        
        // Pool de conexiones LDAP
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "10");
        env.put("com.sun.jndi.ldap.connect.pool.prefsize", "5");
        env.put("com.sun.jndi.ldap.connect. pool.timeout", "300000");

        log("   Conectando a LDAP: " + ldapUrl);
        
        DirContext ctx = null;
        
        try {
            ctx = new InitialDirContext(env);
            log("   ✅ Conexión LDAP establecida");

            // Filtro para Active Directory
            String filter = "(&(objectClass=user)(objectCategory=person)(" + dniAttr + "=" + escapeLDAPSearchFilter(dni) + "))";
            
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setReturningAttributes(new String[] { 
                "cn", "givenName", "sn", "displayName", 
                "mail", "telephoneNumber", "mobile",
                "employeeNumber", "employeeID", 
                "sAMAccountName", "userPrincipalName",
                "department", "title", "physicalDeliveryOfficeName"
            });

            log("   Filtro: " + filter);
            log("   Base DN: " + baseDn);
            
            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, sc);
            
            if (results.hasMore()) {
                SearchResult sr = results.next();
                Attributes attributes = sr.getAttributes();
                
                Map<String,String> map = new HashMap<>();
                
                // Obtener nombre completo (prioridad: displayName > cn > givenName+sn)
                String displayName = getAttr(attributes, "displayName", "");
                String cn = getAttr(attributes, "cn", "");
                String givenName = getAttr(attributes, "givenName", "");
                String sn = getAttr(attributes, "sn", "");
                
                String nombreCompleto = ! displayName.isEmpty() ? displayName : 
                                       !cn.isEmpty() ? cn : 
                                       (givenName + " " + sn). trim();
                
                // Mapear atributos
                map. put("nombreApellidos", nombreCompleto);
                map.put("email", getAttr(attributes, "mail", ""));
                map. put("telefono", getAttr(attributes, "telephoneNumber", getAttr(attributes, "mobile", "")));
                map.put("username", getAttr(attributes, "sAMAccountName", ""));
                map.put("employeeNumber", getAttr(attributes, "employeeNumber", ""));
                map.put("department", getAttr(attributes, "department", ""));
                map. put("title", getAttr(attributes, "title", ""));
                map.put("office", getAttr(attributes, "physicalDeliveryOfficeName", ""));
                
                log("   ✅ Usuario encontrado: " + nombreCompleto);
                if (!map.get("email").isEmpty()) {
                    log("      Email: " + map.get("email"));
                }
                if (!map.get("department").isEmpty()) {
                    log("      Departamento: " + map.get("department"));
                }
                
                return map;
            } else {
                log("   ⚠️ No se encontraron resultados para: " + dni);
                return null;
            }
        } catch (NamingException ne) {
            log("   ❌ Error en búsqueda LDAP: " + ne.getMessage());
            throw ne;
        } finally {
            if (ctx != null) {
                try { 
                    ctx.close();
                    log("   Conexión LDAP cerrada");
                } catch(Exception e) {
                    log("   ⚠️ Error al cerrar contexto LDAP: " + e.getMessage());
                }
            }
        }
    }

    private String getAttr(Attributes attrs, String name, String defaultValue) throws NamingException {
        if (attrs == null) return defaultValue;
        Attribute a = attrs.get(name);
        if (a == null) return defaultValue;
        Object v = a.get();
        return v != null ? v.toString(). trim() : defaultValue;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }

    private String escapeLDAPSearchFilter(String filter) {
        if (filter == null) return "";
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
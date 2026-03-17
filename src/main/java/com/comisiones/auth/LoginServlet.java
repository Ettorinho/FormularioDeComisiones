package com.comisiones.auth;

import com.comisiones.util.AppLogger;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet de login. Valida credenciales contra el Active Directory.
 * GET  → muestra login.jsp
 * POST → autentica y redirige a la app o devuelve error
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private LdapAuthService ldapAuthService;

    @Override
    public void init() throws ServletException {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            String ldapUrl      = (String) envCtx.lookup("ldap/url");
            String baseDn       = (String) envCtx.lookup("ldap/baseDn");
            String bindDn       = (String) envCtx.lookup("ldap/bindDn");
            String bindPassword = (String) envCtx.lookup("ldap/bindPassword");

            String dominio;
            try {
                dominio = (String) envCtx.lookup("ldap/dominio");
            } catch (NamingException e) {
                if (bindDn != null && bindDn.contains("@")) {
                    dominio = bindDn.substring(bindDn.indexOf('@') + 1);
                } else {
                    dominio = extraerDominioDeUrl(ldapUrl);
                }
            }

            ldapAuthService = new LdapAuthService(ldapUrl, baseDn, bindDn, bindPassword, dominio);
            AppLogger.info("LoginServlet inicializado. Dominio AD: " + dominio);

        } catch (NamingException e) {
            throw new ServletException("Error al leer configuración LDAP desde JNDI", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty()) {
            request.setAttribute("error", "El usuario y la contraseña son obligatorios.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        try {
            Map<String, String> datosUsuario = ldapAuthService.autenticar(username.trim(), password);

            if (datosUsuario != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("usuarioLogueado", datosUsuario.get("username"));
                session.setAttribute("nombreCompleto",  datosUsuario.getOrDefault("nombreCompleto", datosUsuario.get("username")));
                session.setAttribute("email",           datosUsuario.getOrDefault("email", ""));
                session.setAttribute("departamento",    datosUsuario.getOrDefault("departamento", ""));
                session.setAttribute("grupos",          datosUsuario.getOrDefault("grupos", ""));
                session.setMaxInactiveInterval(60 * 60);

                AppLogger.info("Login exitoso: " + datosUsuario.get("username"));

                String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
                if (redirectUrl != null) {
                    session.removeAttribute("redirectAfterLogin");
                    response.sendRedirect(redirectUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/");
                }

            } else {
                AppLogger.info("Login fallido para usuario: " + username);
                request.setAttribute("error", "Usuario o contraseña incorrectos. Verifique sus credenciales del Directorio Activo.");
                request.setAttribute("username", username);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }

        } catch (NamingException e) {
            AppLogger.error("Error de conexión LDAP durante login de " + username, e);
            request.setAttribute("error", "No se pudo conectar con el servidor de autenticación. Contacte con el administrador.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private String extraerDominioDeUrl(String ldapUrl) {
        if (ldapUrl == null) return "dominio.local";
        String host = ldapUrl.replaceAll("^ldaps?://", "").replaceAll(":\d+$", "");
        int dotIdx = host.indexOf('.');
        if (dotIdx > 0 && host.indexOf('.', dotIdx + 1) > 0) {
            return host.substring(dotIdx + 1);
        }
        return host;
    }
}
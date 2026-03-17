package com.comisiones.controller;

import com.comisiones.ldap.LdapAuthService;
import com.comisiones.model.UsuarioAD;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet de login. Gestiona el acceso a la aplicación mediante credenciales del AD.
 * GET  /login → muestra el formulario de login (o redirige si ya hay sesión activa).
 * POST /login → valida credenciales y crea la sesión del usuario.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /** Tiempo máximo de sesión: 30 minutos */
    private static final int SESSION_MAX_INACTIVE = 30 * 60;

    private LdapAuthService ldapAuthService;

    @Override
    public void init() throws ServletException {
        log("========================================");
        log("Inicializando LoginServlet...");
        log("========================================");

        try {
            Context initCtx = new InitialContext();
            Context envCtx  = (Context) initCtx.lookup("java:comp/env");

            String ldapUrl    = (String) envCtx.lookup("ldap/url");
            String baseDn     = (String) envCtx.lookup("ldap/baseDn");
            String dominioAd  = (String) envCtx.lookup("ldap/dominio");

            log("📋 Parámetros LDAP para autenticación:");
            log("   ldap/url     = " + (ldapUrl   != null ? ldapUrl   : "❌ NULL"));
            log("   ldap/baseDn  = " + (baseDn    != null ? baseDn    : "❌ NULL"));
            log("   ldap/dominio = " + (dominioAd != null ? dominioAd : "❌ NULL"));

            ldapAuthService = new LdapAuthService(ldapUrl, baseDn, dominioAd);
            log("✅ LoginServlet inicializado correctamente");

        } catch (NamingException e) {
            log("❌ Error al leer configuración LDAP desde JNDI: " + e.getMessage());
            throw new ServletException("No se pudo leer la configuración LDAP desde context.xml (JNDI)", e);
        }
    }

    /**
     * GET /login: muestra el formulario de login o redirige a /comisiones si ya hay sesión.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            // Usuario ya autenticado → redirigir a la aplicación
            response.sendRedirect(request.getContextPath() + "/comisiones");
            return;
        }

        // Mostrar formulario de login
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    /**
     * POST /login: autentica al usuario contra el AD y crea la sesión.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validar que los campos no estén vacíos
        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            log("⚠️ Intento de login con campos vacíos");
            request.setAttribute("error", "Usuario y contraseña son obligatorios.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        username = username.trim();

        try {
            UsuarioAD usuario = ldapAuthService.autenticar(username, password);

            // Login exitoso: crear sesión y guardar el usuario
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(SESSION_MAX_INACTIVE);
            session.setAttribute("usuarioLogueado", usuario);

            log("✅ Login exitoso: " + usuario.getUsername()
                    + " [" + usuario.getNombreCompleto() + "]"
                    + " roles=" + usuario.getRoles());

            log("🔐 Login registrado: " + usuario.getUsername() + " desde IP: " + request.getRemoteAddr());

            // Redirigir a la URL solicitada originalmente o a /comisiones por defecto
            String urlAntesDeSesion = (String) session.getAttribute("urlAntesDeSesion");
            if (urlAntesDeSesion != null && !urlAntesDeSesion.isEmpty()) {
                session.removeAttribute("urlAntesDeSesion");
                response.sendRedirect(urlAntesDeSesion);
            } else {
                response.sendRedirect(request.getContextPath() + "/comisiones");
            }

        } catch (AuthenticationException ae) {
            log("⚠️ Login fallido (credenciales incorrectas): " + username);
            log("🔐 Login fallido registrado para usuario: " + username);
            request.setAttribute("error", "Usuario o contraseña incorrectos.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);

        } catch (NamingException ne) {
            log("❌ Error de conectividad LDAP al intentar login: " + username
                    + " → " + ne.getMessage());
            request.setAttribute("error",
                    "No se pudo conectar con el servidor de autenticación. Inténtelo de nuevo más tarde.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }
}

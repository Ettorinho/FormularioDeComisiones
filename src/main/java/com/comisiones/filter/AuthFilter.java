package com.comisiones.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro de autenticación que protege todas las rutas de la aplicación.
 * Rutas públicas (sin autenticación requerida): /login, /logout y recursos estáticos.
 * Para el resto de rutas, comprueba que exista sesión con el atributo "usuarioLogueado".
 * Si no hay sesión, guarda la URL original y redirige al formulario de login.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Sin inicialización necesaria
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String contextPath = httpReq.getContextPath();
        String requestURI  = httpReq.getRequestURI();

        // Ruta relativa al contexto de la aplicación
        String path = requestURI.substring(contextPath.length());

        // Rutas públicas que no requieren autenticación
        if (esRutaPublica(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Verificar si existe sesión activa con el usuario autenticado
        HttpSession session = httpReq.getSession(false);
        if (session != null && session.getAttribute("usuarioLogueado") != null) {
            chain.doFilter(request, response);
            return;
        }

        // No hay sesión: guardar la URL solicitada y redirigir al login
        HttpSession newSession = httpReq.getSession(true);
        // Solo guardar la URL si no viene de una redirección anterior al login
        String urlOriginal = requestURI;
        if (httpReq.getQueryString() != null && !httpReq.getQueryString().isEmpty()) {
            urlOriginal += "?" + httpReq.getQueryString();
        }
        newSession.setAttribute("urlAntesDeSesion", urlOriginal);
        httpResp.sendRedirect(contextPath + "/login");
    }

    /**
     * Determina si la ruta es pública (no requiere autenticación).
     * Son públicas: /login, /logout y todos los recursos estáticos.
     */
    private boolean esRutaPublica(String path) {
        // Rutas funcionales públicas
        if ("/login".equals(path) || "/logout".equals(path)) {
            return true;
        }

        // Recursos estáticos por prefijo de ruta
        if (path.startsWith("/css/")    || path.startsWith("/js/")
                || path.startsWith("/img/")    || path.startsWith("/images/")
                || path.startsWith("/fonts/")  || path.startsWith("/webjars/")) {
            return true;
        }

        // Recursos estáticos por extensión de archivo
        String pathLower = path.toLowerCase();
        if (pathLower.endsWith(".css")   || pathLower.endsWith(".js")
                || pathLower.endsWith(".png")   || pathLower.endsWith(".jpg")
                || pathLower.endsWith(".ico")   || pathLower.endsWith(".woff")
                || pathLower.endsWith(".woff2")) {
            return true;
        }

        return false;
    }

    @Override
    public void destroy() {
        // Sin recursos que liberar
    }
}

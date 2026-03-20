package com.comisiones.filter;

import com.comisiones.model.UsuarioAD;
import com.comisiones.security.AppRoles;
import com.comisiones.security.RolService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro de autorización por roles.
 * Requiere que AuthFilter ya haya verificado la autenticación.
 *
 * Reglas:
 * - Rutas de solo lectura (GET a /comisiones/*, /miembros, /actas/view, búsquedas): requieren LECTURA
 * - Rutas de gestión de miembros (añadir, quitar, cambiar cargo) y actas: requieren GESTOR
 * - Rutas de creación/formulario de comisiones: requieren ADMIN
 * - Usuario sin ningún rol → página sin-permisos
 */
public class RolFilter implements Filter {

    private RolService rolService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        rolService = new RolService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String contextPath = httpReq.getContextPath();
        String requestURI  = httpReq.getRequestURI();
        String path        = requestURI.substring(contextPath.length());
        String method      = httpReq.getMethod();

        // Rutas públicas (login, logout, recursos estáticos) → pasar sin verificar rol
        if (esRutaPublica(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Obtener usuario de sesión (AuthFilter ya garantiza que existe)
        HttpSession session = httpReq.getSession(false);
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }
        UsuarioAD usuario = (UsuarioAD) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            chain.doFilter(request, response);
            return;
        }

        // Resolver rol del usuario y guardarlo en sesión para los JSP
        String rolUsuario = rolService.resolverRol(usuario);
        session.setAttribute("rolUsuario", rolUsuario);
        System.out.println("[RolFilter] Usuario: " + usuario.getUsername()
                + " | Grupos AD: " + usuario.getRoles()
                + " | Rol resuelto: " + rolUsuario);

        // Verificar permisos según la ruta y el método HTTP
        String rolRequerido = determinarRolRequerido(path, method);
        if (!rolService.tienePermiso(usuario, rolRequerido)) {
            httpResp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpReq.setAttribute("rolRequerido", rolRequerido);
            httpReq.setAttribute("rolUsuario", rolUsuario);
            httpReq.getRequestDispatcher("/WEB-INF/views/sin-permisos.jsp").forward(httpReq, httpResp);
            return;
        }

        chain.doFilter(request, response);
    }

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

    private String determinarRolRequerido(String path, String method) {
        // Rutas que requieren ADMIN
        if ("GET".equals(method) && path.startsWith("/comisiones/new")) {
            return AppRoles.ADMIN;
        }
        if ("POST".equals(method) && (path.equals("/comisiones") || path.equals("/comisiones/"))) {
            return AppRoles.ADMIN;
        }

        // Rutas que requieren GESTOR
        if (path.startsWith("/comisiones/") && (
                path.contains("/addMember") ||
                path.contains("/bajaMiembro") ||
                path.contains("/bajaMiembros") ||
                path.contains("/cambiarCargo"))) {
            return AppRoles.GESTOR;
        }
        if (path.startsWith("/actas") && "POST".equals(method)) {
            return AppRoles.GESTOR;
        }
        if (path.startsWith("/actas/new") || path.startsWith("/actas/save")) {
            return AppRoles.GESTOR;
        }

        // Todo lo demás requiere solo LECTURA (acceso autenticado básico)
        return AppRoles.LECTURA;
    }

    @Override
    public void destroy() {}
}

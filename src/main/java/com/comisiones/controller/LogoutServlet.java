package com.comisiones.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet de logout. Invalida la sesión del usuario y redirige al formulario de login.
 * GET /logout → cierra la sesión y redirige a /login.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = "";
            Object usuario = session.getAttribute("usuarioLogueado");
            if (usuario instanceof com.comisiones.model.UsuarioAD) {
                username = ((com.comisiones.model.UsuarioAD) usuario).getUsername();
            }
            session.invalidate();
            log("✅ Sesión cerrada para el usuario: " + username);
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}

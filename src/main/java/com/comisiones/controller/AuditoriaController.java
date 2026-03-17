package com.comisiones.controller;

import com.comisiones.dao.AuditoriaDAO;
import com.comisiones.model.AuditoriaAccion;
import com.comisiones.util.AppLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet para consultar el historial de auditoría.
 * GET /auditoria              → lista las últimas 200 acciones
 * GET /auditoria?usuario=xxx  → filtra por usuario
 * GET /auditoria?entidad=xxx&entidadId=yyy → filtra por entidad
 */
@WebServlet("/auditoria")
public class AuditoriaController extends HttpServlet {

    private AuditoriaDAO auditoriaDAO;

    @Override
    public void init() throws ServletException {
        auditoriaDAO = new AuditoriaDAO();
        AppLogger.info("AuditoriaController inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String usuario   = request.getParameter("usuario");
            String entidad   = request.getParameter("entidad");
            String entidadId = request.getParameter("entidadId");

            List<AuditoriaAccion> acciones;

            if (usuario != null && !usuario.trim().isEmpty()) {
                acciones = auditoriaDAO.findByUsuario(usuario.trim());
                request.setAttribute("filtroUsuario", usuario.trim());
            } else if (entidad != null && entidadId != null
                    && !entidad.trim().isEmpty() && !entidadId.trim().isEmpty()) {
                acciones = auditoriaDAO.findByEntidad(entidad.trim(), entidadId.trim());
                request.setAttribute("filtroEntidad", entidad.trim());
                request.setAttribute("filtroEntidadId", entidadId.trim());
            } else {
                acciones = auditoriaDAO.findAll(200);
            }

            request.setAttribute("acciones", acciones);
            request.getRequestDispatcher("/WEB-INF/views/auditoria/list.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            AppLogger.error("Error al consultar auditoría", e);
            throw new ServletException("Error al consultar auditoría", e);
        }
    }
}

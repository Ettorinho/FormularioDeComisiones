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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
            String usuario = limpiar(request.getParameter("usuario"));
            String resultado = limpiar(request.getParameter("resultado"));
            String fechaDesdeStr = limpiar(request.getParameter("fechaDesde"));
            String fechaHastaStr = limpiar(request.getParameter("fechaHasta"));
            String ipAddress = limpiar(request.getParameter("ip"));

            LocalDate fechaDesde = parsearFecha(fechaDesdeStr);
            LocalDate fechaHasta = parsearFecha(fechaHastaStr);

            List<AuditoriaAccion> acciones = auditoriaDAO.findByFiltros(
                    usuario, resultado, fechaDesde, fechaHasta, ipAddress, 200);

            request.setAttribute("acciones", acciones);
            request.setAttribute("filtroUsuario", usuario);
            request.setAttribute("filtroResultado", resultado);
            request.setAttribute("filtroFechaDesde", fechaDesdeStr);
            request.setAttribute("filtroFechaHasta", fechaHastaStr);
            request.setAttribute("filtroIp", ipAddress);

            request.getRequestDispatcher("/WEB-INF/views/admin/auditoria.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            AppLogger.error("Error al consultar auditoría", e);
            throw new ServletException("Error al consultar auditoría", e);
        }
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String clean = valor.trim();
        return clean.isEmpty() ? null : clean;
    }

    private LocalDate parsearFecha(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}

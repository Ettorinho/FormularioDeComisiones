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
 * Servlet para consultar el historial de auditoría con paginación y filtros.
 * GET /auditoria                                     → lista la primera página
 * GET /auditoria?usuario=xxx                         → filtra por usuario
 * GET /auditoria?resultado=FALLIDO                   → filtra por resultado
 * GET /auditoria?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD → filtra por rango de fechas
 * Los filtros son combinables y soportan paginación mediante el parámetro page.
 */
@WebServlet("/auditoria")
public class AuditoriaController extends HttpServlet {

    private static final int TAMANO_PAGINA = 50;

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
            String filtroUsuario   = emptyToNull(request.getParameter("usuario"));
            String filtroResultado = emptyToNull(request.getParameter("resultado"));
            String fechaDesde      = emptyToNull(request.getParameter("fechaDesde"));
            String fechaHasta      = emptyToNull(request.getParameter("fechaHasta"));

            // Validar resultado si se proporcionó
            if (filtroResultado != null) {
                try {
                    AuditoriaDAO.Resultado.valueOf(filtroResultado.toUpperCase());
                    filtroResultado = filtroResultado.toUpperCase();
                } catch (IllegalArgumentException e) {
                    filtroResultado = null;
                }
            }

            // Paginación
            int pagina = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null) {
                try {
                    int parsed = Integer.parseInt(pageParam);
                    if (parsed >= 1) pagina = parsed;
                } catch (NumberFormatException e) {
                    // valor inválido → página 1
                }
            }

            long total = auditoriaDAO.countFiltrado(filtroUsuario, filtroResultado, fechaDesde, fechaHasta);
            int totalPaginas = (total == 0) ? 1 : (int) Math.ceil((double) total / TAMANO_PAGINA);
            if (pagina > totalPaginas) pagina = totalPaginas;

            List<AuditoriaAccion> acciones = auditoriaDAO.findPaginado(
                    filtroUsuario, filtroResultado, fechaDesde, fechaHasta, pagina, TAMANO_PAGINA);

            request.setAttribute("acciones",       acciones);
            request.setAttribute("paginaActual",   pagina);
            request.setAttribute("totalPaginas",   totalPaginas);
            request.setAttribute("totalRegistros", total);
            request.setAttribute("filtroUsuario",   filtroUsuario   != null ? filtroUsuario   : "");
            request.setAttribute("filtroResultado", filtroResultado != null ? filtroResultado : "");
            request.setAttribute("fechaDesde",      fechaDesde      != null ? fechaDesde      : "");
            request.setAttribute("fechaHasta",      fechaHasta      != null ? fechaHasta      : "");

            request.getRequestDispatcher("/WEB-INF/views/auditoria/list.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            AppLogger.error("Error al consultar auditoría", e);
            throw new ServletException("Error al consultar auditoría", e);
        }
    }

    private String emptyToNull(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}

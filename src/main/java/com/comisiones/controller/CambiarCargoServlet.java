package com.comisiones.controller;

import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.HistorialCargoDAO;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.HistorialCargo;
import com.comisiones.model.UsuarioAD;
import com.comisiones.service.AuditoriaService;
import com.comisiones.util.ServletHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet para gestionar el cambio de cargo de miembros en comisiones.
 * Permite cambiar el cargo de un miembro manteniendo su historial completo.
 */
@WebServlet("/comisiones/cambiarCargo")
public class CambiarCargoServlet extends HttpServlet {
    
    private ComisionMiembroDAO comisionMiembroDAO;
    private HistorialCargoDAO historialDAO;
    
    @Override
    public void init() {
        comisionMiembroDAO = new ComisionMiembroDAO();
        historialDAO = new HistorialCargoDAO();
    }

    private String getUsuarioLogueado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            UsuarioAD u = (UsuarioAD) session.getAttribute("usuarioLogueado");
            if (u != null) return u.getUsername();
        }
        return "SISTEMA";
    }

    /**
     * GET: Muestra el formulario de cambio de cargo con el historial.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String comisionIdStr = request.getParameter("comisionId");
        String miembroIdStr = request.getParameter("miembroId");
        
        // Validar parámetros requeridos
        if (comisionIdStr == null || miembroIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                "Parámetros requeridos: comisionId y miembroId");
            return;
        }
        
        Long comisionId = ServletHelper.parseIdSafely(comisionIdStr);
        Long miembroId = ServletHelper.parseIdSafely(miembroIdStr);
        
        if (comisionId == null || miembroId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "IDs inválidos");
            return;
        }
        
        try {
            // Obtener ComisionMiembro por clave compuesta
            ComisionMiembro cm = comisionMiembroDAO.findByCompositeKey(comisionId, miembroId);
            if (cm == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "No se encontró el miembro en esta comisión");
                return;
            }
            
            // Obtener historial de cambios
            List<HistorialCargo> historial = historialDAO.getHistorialByComisionMiembro(comisionId, miembroId);
            
            // Pasar datos a la vista
            request.setAttribute("comisionMiembro", cm);
            request.setAttribute("historial", historial);
            
            request.getRequestDispatcher("/WEB-INF/views/comisiones/cambiarCargo.jsp")
                   .forward(request, response);
            
        } catch (SQLException e) {
            throw new ServletException("Error al obtener datos del miembro", e);
        }
    }
    
    /**
     * POST: Procesa el cambio de cargo.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String comisionIdStr = request.getParameter("comisionId");
        String miembroIdStr = request.getParameter("miembroId");
        String nuevoCargo = request.getParameter("nuevoCargo");
        String motivo = request.getParameter("motivo");
        
        // Validar parámetros obligatorios
        if (comisionIdStr == null || miembroIdStr == null || 
            nuevoCargo == null || nuevoCargo.trim().isEmpty()) {
            request.setAttribute("error", "Todos los campos obligatorios son requeridos");
            doGet(request, response);
            return;
        }
        
        Long comisionId = ServletHelper.parseIdSafely(comisionIdStr);
        Long miembroId = ServletHelper.parseIdSafely(miembroIdStr);
        
        if (comisionId == null || miembroId == null) {
            request.setAttribute("error", "IDs inválidos");
            doGet(request, response);
            return;
        }
        
        try {
            // Validar que el miembro existe y no está dado de baja
            ComisionMiembro cm = comisionMiembroDAO.findByCompositeKey(comisionId, miembroId);
            if (cm == null) {
                request.setAttribute("error", "No se encontró el miembro en esta comisión");
                doGet(request, response);
                return;
            }
            
            if (cm.getFechaBaja() != null) {
                request.setAttribute("error", "No se puede cambiar el cargo de un miembro dado de baja");
                doGet(request, response);
                return;
            }
            
            // Validar que el cargo sea diferente al actual
            if (cm.getCargo().name().equals(nuevoCargo)) {
                request.setAttribute("error", "El cargo seleccionado es el mismo que el actual");
                doGet(request, response);
                return;
            }
            
            // Cambiar cargo y registrar motivo en una única transacción atómica
            HttpSession session = request.getSession(false);
            UsuarioAD usuarioLogueado = (session != null)
                ? (UsuarioAD) session.getAttribute("usuarioLogueado")
                : null;
            String usernameAD = (usuarioLogueado != null) ? usuarioLogueado.getUsername() : "SYSTEM";
            boolean success = comisionMiembroDAO.cambiarCargoConMotivo(
                    comisionId, miembroId, nuevoCargo, motivo, usernameAD);
            
            if (success) {
                AuditoriaService.getInstance().registrar(request, getUsuarioLogueado(request),
                    "MODIFICAR", "CARGO", comisionId + "/" + miembroId,
                    "Cambió el cargo de " + cm.getCargo().name() + " a " + nuevoCargo
                        + " en la comisión ID: " + comisionId);
                request.setAttribute("success", "Cargo cambiado exitosamente de " + 
                    cm.getCargo().name() + " a " + nuevoCargo);
            } else {
                request.setAttribute("error", "No se pudo cambiar el cargo. Inténtelo nuevamente.");
            }
            
            // Recargar la página con los datos actualizados
            doGet(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID inválido");
            doGet(request, response);
        } catch (SQLException e) {
            throw new ServletException("Error al cambiar el cargo", e);
        }
    }
}

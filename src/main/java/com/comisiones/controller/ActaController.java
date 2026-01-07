package com.comisiones.controller;

import com.comisiones.dao.ActaDAO;
import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class ActaController extends HttpServlet {
    
    private ActaDAO actaDAO;
    private ComisionDAO comisionDAO;
    private ComisionMiembroDAO comisionMiembroDAO;
    
    @Override
    public void init() {
        actaDAO = new ActaDAO();
        comisionDAO = new ComisionDAO();
        comisionMiembroDAO = new ComisionMiembroDAO();
        System.out.println("--- ActaController INICIALIZADO ---");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo. equals("/")) {
            pathInfo = "/new";
        }
        
        try {
            if (pathInfo.equals("/new")) {
                showNewActaForm(request, response);
            } else if (pathInfo.equals("/view")) {
                viewActa(request, response);
            } else if (pathInfo.equals("/list")) {
                listActas(request, response);
            } else if (pathInfo.equals("/loadMiembros")) {  // ⭐ AGREGADO
                loadMiembrosComision(request, response);
            } else {
                showNewActaForm(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Error en operación GET", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.equals("/save")) {
                saveActa(request, response);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            throw new ServletException("Error en operación POST", e);
        }
    }
    
    private void showNewActaForm(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        // Obtener solo comisiones activas
        List<Comision> comisiones = comisionDAO.findAll();
        List<Comision> comisionesActivas = new ArrayList<>();
        
        Date hoy = new Date();
        for (Comision c : comisiones) {
            if (c.getFechaFin() == null || c.getFechaFin().after(hoy)) {
                comisionesActivas. add(c);
            }
        }
        
        request.setAttribute("comisiones", comisionesActivas);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/actas/form.jsp");
        dispatcher.forward(request, response);
    }
    
    private void loadMiembrosComision(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        
        System.out.println("========================================");
        System.out.println("===> loadMiembrosComision INICIADO");
        System.out. println("========================================");
        
        String comisionIdStr = request.getParameter("comisionId");
        System.out.println("===> Parámetro comisionId: " + comisionIdStr);
        
        if (comisionIdStr == null || comisionIdStr.isEmpty()) {
            System.out. println("===> ERROR: comisionId vacío o null");
            response.getWriter().write("<p class='text-danger'>ID de comisión no válido</p>");
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdStr);
        System.out.println("===> ComisionId parseado: " + comisionId);
        
        List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(comisionId);
        System.out.println("===> Total miembros encontrados: " + miembros.size());
        
        // Filtrar solo miembros activos (sin fecha de baja)
        List<ComisionMiembro> miembrosActivos = new ArrayList<>();
        for (ComisionMiembro cm : miembros) {
            if (cm.getFechaBaja() == null) {
                miembrosActivos.add(cm);
                System.out.println("===>   - Miembro activo: " + cm.getMiembro().getNombreApellidos());
            } else {
                System.out. println("===>   - Miembro inactivo (ignorado): " + cm.getMiembro().getNombreApellidos());
            }
        }
        
        System.out.println("===> Total miembros ACTIVOS: " + miembrosActivos.size());
        
        request.setAttribute("miembros", miembrosActivos);
        
        String rutaFragment = "/WEB-INF/views/actas/miembros-fragment.jsp";
        System.out.println("===> Ruta del JSP:  " + rutaFragment);
        System.out.println("===> Context path: " + request.getContextPath());
        System.out.println("===> Servlet path: " + request.getServletPath());
        
        RequestDispatcher dispatcher = request.getRequestDispatcher(rutaFragment);
        
        if (dispatcher == null) {
            System.out.println("===> ERROR:   RequestDispatcher es NULL");
            response.getWriter().write("<p class='text-danger'>Error:  No se pudo cargar el fragment</p>");
            return;
        }
        
        System.out.println("===> Forwarding a:  " + rutaFragment);
        dispatcher.forward(request, response);
        System.out.println("===> Forward completado");
        System.out.println("========================================");
    }
    
    private void saveActa(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ParseException, IOException, ServletException {
        
        System.out.println("===> Guardando acta...");
        
        String comisionIdStr = request.getParameter("comisionId");
        String fechaReunionStr = request.getParameter("fechaReunion");
        String observaciones = request.getParameter("observaciones");
        String[] miembrosAsistieron = request.getParameterValues("asistio");
        
        System.out.println("Comisión ID: " + comisionIdStr);
        System.out.println("Fecha reunión: " + fechaReunionStr);
        System.out.println("Observaciones: " + observaciones);
        System.out.println("Miembros que asistieron: " + (miembrosAsistieron != null ? miembrosAsistieron.length : 0));
        
        // Validaciones
        if (comisionIdStr == null || fechaReunionStr == null) {
            request.setAttribute("error", "Debe seleccionar comisión y fecha de reunión");
            showNewActaForm(request, response);
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdStr);
        
        // Crear acta
        Comision comision = comisionDAO. findById(comisionId);
        Date fechaReunion = new SimpleDateFormat("yyyy-MM-dd").parse(fechaReunionStr);
        
        Acta acta = new Acta(comision, fechaReunion, observaciones);
        actaDAO.save(acta);
        
        System.out.println("Acta guardada con ID: " + acta.getId());
        
        // Guardar asistencias
        List<ComisionMiembro> miembros = comisionMiembroDAO. findByComisionId(comisionId);
        int asistenciasGuardadas = 0;
        
        for (ComisionMiembro cm : miembros) {
            if (cm.getFechaBaja() == null) { // Solo miembros activos
                boolean asistio = false;
                
                if (miembrosAsistieron != null) {
                    for (String miembroIdStr : miembrosAsistieron) {
                        if (Long.parseLong(miembroIdStr) == cm.getMiembro().getId()) {
                            asistio = true;
                            break;
                        }
                    }
                }
                
                AsistenciaActa asistencia = new AsistenciaActa(acta, cm. getMiembro(), asistio);
                actaDAO.saveAsistencia(asistencia);
                asistenciasGuardadas++;
            }
        }
        
        System.out.println("Asistencias guardadas:  " + asistenciasGuardadas);
        
        response.sendRedirect(request.getContextPath() + "/actas/view?id=" + acta.getId());
    }
    
    private void viewActa(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        String idStr = request.getParameter("id");
        
        if (idStr == null) {
            request.setAttribute("error", "ID de acta no especificado");
            showNewActaForm(request, response);
            return;
        }
        
        Long id = Long.parseLong(idStr);
        Acta acta = actaDAO.findById(id);
        
        if (acta == null) {
            request.setAttribute("error", "Acta no encontrada");
            showNewActaForm(request, response);
            return;
        }
        
        List<AsistenciaActa> asistencias = actaDAO.findAsistenciasByActaId(id);
        
        request.setAttribute("acta", acta);
        request.setAttribute("asistencias", asistencias);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/actas/view.jsp");
        dispatcher.forward(request, response);
    }
    
    private void listActas(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        List<Acta> actas = actaDAO.findAll();
        request.setAttribute("actas", actas);
        
        RequestDispatcher dispatcher = request. getRequestDispatcher("/WEB-INF/views/actas/list.jsp");
        dispatcher. forward(request, response);
    }
}
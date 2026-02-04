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
        
        if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/new";
        }
        
        try {
            if (pathInfo.equals("/new")) {
                showNewActaForm(request, response);
            } else if (pathInfo.equals("/view")) {
                viewActa(request, response);
            } else if (pathInfo.equals("/list")) {
                listActas(request, response);
            } else if (pathInfo.equals("/loadMiembros")) {
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
        List<Comision> comisiones = comisionDAO.findAll();
        List<Comision> comisionesActivas = new ArrayList<>();
        
        Date hoy = new Date();
        for (Comision c : comisiones) {
            if (c.getFechaFin() == null || c.getFechaFin().after(hoy)) {
                comisionesActivas.add(c);
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
        System.out.println("========================================");
        
        String comisionIdStr = request.getParameter("comisionId");
        System.out.println("===> Parámetro comisionId: " + comisionIdStr);
        
        if (comisionIdStr == null || comisionIdStr.isEmpty()) {
            System.out.println("===> ERROR: comisionId vacío o null");
            response.getWriter().write("<p class='text-danger'>ID de comisión no válido</p>");
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdStr);
        System.out.println("===> ComisionId parseado: " + comisionId);
        
        List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(comisionId);
        System.out.println("===> Total miembros encontrados: " + miembros.size());
        
        List<ComisionMiembro> miembrosActivos = new ArrayList<>();
        for (ComisionMiembro cm : miembros) {
            if (cm.getFechaBaja() == null) {
                miembrosActivos.add(cm);
                System.out.println("===>   - Miembro activo: " + cm.getMiembro().getNombreApellidos());
            } else {
                System.out.println("===>   - Miembro inactivo (ignorado): " + cm.getMiembro().getNombreApellidos());
            }
        }
        
        System.out.println("===> Total miembros ACTIVOS: " + miembrosActivos.size());
        
        request.setAttribute("miembros", miembrosActivos);
        
        String rutaFragment = "/WEB-INF/views/actas/miembros-fragment.jsp";
        System.out.println("===> Ruta del JSP: " + rutaFragment);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher(rutaFragment);
        
        if (dispatcher == null) {
            System.out.println("===> ERROR: RequestDispatcher es NULL");
            response.getWriter().write("<p class='text-danger'>Error: No se pudo cargar el fragment</p>");
            return;
        }
        
        System.out.println("===> Forwarding a: " + rutaFragment);
        dispatcher.forward(request, response);
        System.out.println("===> Forward completado");
        System.out.println("========================================");
    }
    
    private void saveActa(HttpServletRequest request, HttpServletResponse response) 
        throws SQLException, ParseException, IOException, ServletException {
    
    System.out.println("\n========================================");
    System.out.println("===> GUARDANDO ACTA - DEBUG COMPLETO");
    System.out.println("========================================\n");
    
    // Mostrar TODOS los parámetros recibidos
    System.out.println("=== TODOS LOS PARÁMETROS RECIBIDOS ===");
    java.util.Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String paramName = paramNames.nextElement();
        String[] paramValues = request.getParameterValues(paramName);
        for (String paramValue : paramValues) {
            System.out.println("  " + paramName + " = " + paramValue);
        }
    }
    System.out.println("=====================================\n");
    
    String comisionIdStr = request.getParameter("comisionId");
    String fechaReunionStr = request.getParameter("fechaReunion");
    String observaciones = request.getParameter("observaciones");
    String[] miembroIds = request.getParameterValues("miembroId");
    
    System.out.println("=== PARÁMETROS PRINCIPALES ===");
    System.out.println("Comisión ID: " + comisionIdStr);
    System.out.println("Fecha reunión: " + fechaReunionStr);
    System.out.println("Observaciones: " + (observaciones != null ? observaciones.substring(0, Math.min(50, observaciones.length())) : "null"));
    System.out.println("Número de miembros: " + (miembroIds != null ? miembroIds.length : 0));
    
    if (miembroIds != null) {
        System.out.println("IDs de miembros: " + java.util.Arrays.toString(miembroIds));
    }
    System.out.println("==============================\n");
    
    // Validaciones
    if (comisionIdStr == null || comisionIdStr.isEmpty()) {
        System.out.println("ERROR: comisionId es null o vacío");
        request.setAttribute("error", "Debe seleccionar una comisión");
        showNewActaForm(request, response);
        return;
    }
    
    if (fechaReunionStr == null || fechaReunionStr.isEmpty()) {
        System.out.println("ERROR: fechaReunion es null o vacía");
        request.setAttribute("error", "Debe seleccionar una fecha de reunión");
        showNewActaForm(request, response);
        return;
    }
    
    Long comisionId = Long.parseLong(comisionIdStr);
    
    // Buscar la comisión
    Comision comision = comisionDAO.findById(comisionId);
    
    if (comision == null) {
        System.out.println("ERROR: No se encontró la comisión con ID " + comisionId);
        request.setAttribute("error", "La comisión seleccionada no existe");
        showNewActaForm(request, response);
        return;
    }
    
    System.out.println("✅ Comisión encontrada: " + comision.getNombre());
    
    // Parsear fecha
    Date fechaReunion = new SimpleDateFormat("yyyy-MM-dd").parse(fechaReunionStr);
    System.out.println("✅ Fecha parseada: " + fechaReunion);
    
    // Crear acta
    Acta acta = new Acta(comision, fechaReunion, observaciones);
    actaDAO.save(acta);
    
    System.out.println("\n✅ ACTA GUARDADA CON ID: " + acta.getId() + "\n");
    
    // Guardar asistencias
    if (miembroIds != null && miembroIds.length > 0) {
        System.out.println("=== GUARDANDO ASISTENCIAS ===");
        
        int asistenciasGuardadas = 0;
        int asistieron = 0;
        int noAsistieron = 0;
        int conJustificacion = 0;
        
        for (String miembroIdStr : miembroIds) {
            Long miembroId = Long.parseLong(miembroIdStr);
            
            // Obtener parámetros de asistencia
            String asistenciaValor = request.getParameter("asistencia_" + miembroId);
            String justificacion = request.getParameter("justificacion_" + miembroId);
            
            System.out.println("\n--- Miembro ID: " + miembroId + " ---");
            System.out.println("  Radio seleccionado: " + asistenciaValor);
            System.out.println("  Justificación recibida: " + (justificacion != null ? "'" + justificacion + "'" : "null"));
            
            // Determinar si asistió
            boolean asistio = "ASISTIO".equals(asistenciaValor);
            
            // Si asistió, no guardar justificación
            if (asistio) {
                justificacion = null;
                asistieron++;
                System.out.println("  ✅ ASISTIÓ - justificación = null");
            } else if ("NO_ASISTIO".equals(asistenciaValor)) {
                noAsistieron++;
                // Limpiar justificación vacía
                if (justificacion != null && justificacion.trim().isEmpty()) {
                    justificacion = null;
                    System.out.println("  ❌ NO ASISTIÓ - sin justificación");
                } else if (justificacion != null) {
                    conJustificacion++;
                    System.out.println("  ❌ NO ASISTIÓ - con justificación: '" + justificacion + "'");
                } else {
                    System.out.println("  ❌ NO ASISTIÓ - sin justificación");
                }
            } else {
                System.out.println("  ⚠️ ADVERTENCIA: No se seleccionó asistencia para este miembro");
                // Si no se seleccionó nada, saltamos este miembro
                continue;
            }
            
            // Crear objeto Miembro
            Miembro miembro = new Miembro();
            miembro.setId(miembroId);
            
            // Crear objeto AsistenciaActa
            AsistenciaActa asistencia = new AsistenciaActa(acta, miembro, asistio, justificacion);
            
            System.out.println("  Guardando en BD:");
            System.out.println("    - acta_id: " + acta.getId());
            System.out.println("    - miembro_id: " + miembroId);
            System.out.println("    - asistio: " + asistio);
            System.out.println("    - justificacion: " + (justificacion != null ? "'" + justificacion + "'" : "NULL"));
            
            try {
                actaDAO.saveAsistencia(asistencia);
                asistenciasGuardadas++;
                System.out.println("  ✅ Asistencia guardada correctamente con ID: " + asistencia.getId());
            } catch (Exception e) {
                System.err.println("  ❌ ERROR al guardar asistencia:");
                e.printStackTrace();
            }
        }
        
        System.out.println("\n=== RESUMEN DE ASISTENCIAS ===");
        System.out.println("Total procesados: " + miembroIds.length);
        System.out.println("Guardadas en BD: " + asistenciasGuardadas);
        System.out.println("Asistieron: " + asistieron);
        System.out.println("No asistieron: " + noAsistieron);
        System.out.println("Con justificación: " + conJustificacion);
        System.out.println("==============================\n");
        
    } else {
        System.out.println("⚠️ ADVERTENCIA: No hay miembros para guardar asistencias\n");
    }
    
    System.out.println("========================================");
    System.out.println("===> ✅ PROCESO COMPLETADO");
    System.out.println("========================================\n");
    
    response.sendRedirect(request.getContextPath() + "/actas/view?id=" + acta.getId());
}
    
    private void viewActa(HttpServletRequest request, HttpServletResponse response) 
        throws SQLException, ServletException, IOException {
    
    System.out.println("\n========================================");
    System.out.println("===> VIEW ACTA INICIADO");
    System.out.println("========================================");
    
    String idStr = request.getParameter("id");
    System.out.println("ID del acta solicitada: " + idStr);
    
    if (idStr == null) {
        System.out.println("ERROR: ID es null");
        request.setAttribute("error", "ID de acta no especificado");
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    
    Long id = Long.parseLong(idStr);
    System.out.println("ID parseado: " + id);
    
    // Buscar acta
    Acta acta = actaDAO.findById(id);
    System.out.println("Acta encontrada: " + (acta != null ? "SÍ" : "NO"));
    
    if (acta == null) {
        System.out.println("ERROR: Acta no encontrada con ID " + id);
        request.setAttribute("error", "Acta no encontrada");
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    
    System.out.println("Comisión: " + acta.getComision().getNombre());
    System.out.println("Fecha reunión: " + acta.getFechaReunion());
    System.out.println("Observaciones: " + (acta.getObservaciones() != null ? acta.getObservaciones().substring(0, Math.min(50, acta.getObservaciones().length())) : "null"));
    
    // Buscar asistencias
    System.out.println("\n--- Buscando asistencias para acta ID: " + id);
    List<AsistenciaActa> asistencias = actaDAO.findAsistenciasByActaId(id);
    
    System.out.println("Asistencias encontradas: " + (asistencias != null ? asistencias.size() : "NULL"));
    
    if (asistencias != null && !asistencias.isEmpty()) {
        System.out.println("\n=== DETALLE DE ASISTENCIAS ===");
        for (int i = 0; i < asistencias.size(); i++) {
            AsistenciaActa a = asistencias.get(i);
            System.out.println((i+1) + ". " + a.getMiembro().getNombreApellidos());
            System.out.println("   - Asistió: " + a.isAsistio());
            System.out.println("   - Justificación: " + (a.getJustificacion() != null ? "'" + a.getJustificacion() + "'" : "NULL"));
        }
        System.out.println("==============================\n");
    } else {
        System.out.println("⚠️ NO hay asistencias o la lista es null");
    }
    
    // Establecer atributos
    request.setAttribute("acta", acta);
    request.setAttribute("asistencias", asistencias);
    
    System.out.println("\nAtributos establecidos:");
    System.out.println("  - acta: " + (request.getAttribute("acta") != null ? "OK" : "NULL"));
    System.out.println("  - asistencias: " + (request.getAttribute("asistencias") != null ? "OK" : "NULL"));
    
    // Forward a la vista
    String jspPath = "/WEB-INF/views/actas/view.jsp";
    System.out.println("\nForwarding a: " + jspPath);
    
    RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
    dispatcher.forward(request, response);
    
    System.out.println("========================================");
    System.out.println("===> VIEW ACTA COMPLETADO");
    System.out.println("========================================\n");
}
    
    private void listActas(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        List<Acta> actas = actaDAO.findAll();
        request.setAttribute("actas", actas);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/actas/list.jsp");
        dispatcher.forward(request, response);
    }
}
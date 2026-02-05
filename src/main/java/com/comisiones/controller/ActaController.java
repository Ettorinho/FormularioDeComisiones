package com.comisiones.controller;

import com.comisiones.dao.ActaDAO;
import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/actas/*")
@MultipartConfig(
    maxFileSize = 5 * 1024 * 1024,      // 5MB máximo por archivo
    maxRequestSize = 10 * 1024 * 1024,  // 10MB máximo request
    fileSizeThreshold = 1024 * 1024     // 1MB threshold para memoria
)
public class ActaController extends HttpServlet {
    
    private ActaDAO actaDAO;
    private ComisionDAO comisionDAO;
    private MiembroDAO miembroDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        actaDAO = new ActaDAO();
        comisionDAO = new ComisionDAO();
        miembroDAO = new MiembroDAO();
        System.out.println("--- ActaController INICIALIZADO ---");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/new")) {
                showForm(request, response);
            } else if (pathInfo.equals("/view")) {
                viewActa(request, response);
            } else if (pathInfo.equals("/loadMiembros")) {
                loadMiembrosComision(request, response);
            } else if (pathInfo.equals("/download-pdf")) {
                downloadPdf(request, response);
            } else if (pathInfo.equals("/view-pdf")) {
                viewPdf(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.equals("/save")) {
                saveActa(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
    
    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        
        // Usar el método que tengas disponible en ComisionDAO
        List<Comision> comisiones = comisionDAO.findAll();
        request.setAttribute("comisiones", comisiones);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/actas/form.jsp");
        dispatcher.forward(request, response);
    }
    
    private void loadMiembrosComision(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        
        System.out.println("========================================");
        System.out.println("===> loadMiembrosComision INICIADO");
        System.out.println("========================================");
        
        String comisionIdParam = request.getParameter("comisionId");
        System.out.println("===> Parámetro comisionId: " + comisionIdParam);
        
        if (comisionIdParam == null || comisionIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdParam);
        System.out.println("===> ComisionId parseado: " + comisionId);
        
        // Usar el método que tengas disponible en MiembroDAO
        // Intenta primero con findMiembrosByComisionId
        List<Miembro> miembros = null;
        
        try {
            miembros = miembroDAO.findMiembrosByComisionId(comisionId);
        } catch (Exception e) {
            System.out.println("⚠️ Método findMiembrosByComisionId no disponible");
            // Si no existe ese método, devuelve lista vacía
            miembros = new ArrayList<>();
        }
        
        System.out.println("===> Total miembros encontrados: " + (miembros != null ? miembros.size() : 0));
        
        if (miembros != null) {
            for (Miembro m : miembros) {
                System.out.println("===>   - Miembro: " + m.getNombreApellidos());
            }
        }
        
        request.setAttribute("miembros", miembros);
        
        String jspPath = "/WEB-INF/views/actas/miembros-fragment.jsp";
        System.out.println("===> Ruta del JSP: " + jspPath);
        System.out.println("===> Forwarding a: " + jspPath);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
        
        System.out.println("===> Forward completado");
        System.out.println("========================================\n");
    }
    
    private void saveActa(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        System.out.println("\n========================================");
        System.out.println("===> GUARDANDO ACTA - DEBUG COMPLETO");
        System.out.println("========================================\n");
        
        // Debug: Mostrar TODOS los parámetros
        System.out.println("=== TODOS LOS PARÁMETROS RECIBIDOS ===");
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            System.out.println("  " + paramName + " = " + paramValue);
        }
        System.out.println("=====================================\n");
        
        // Obtener parámetros principales
        String comisionIdStr = request.getParameter("comisionId");
        String fechaReunionStr = request.getParameter("fechaReunion");
        String observaciones = request.getParameter("observaciones");
        
        // Procesar archivo PDF
        Part pdfPart = null;
        String pdfNombre = null;
        byte[] pdfContenido = null;
        String pdfTipoMime = null;
        
        try {
            pdfPart = request.getPart("pdfFile");
            
            if (pdfPart != null && pdfPart.getSize() > 0) {
                pdfNombre = getFileName(pdfPart);
                pdfTipoMime = pdfPart.getContentType();
                
                System.out.println("=== ARCHIVO PDF DETECTADO ===");
                System.out.println("  Nombre: " + pdfNombre);
                System.out.println("  Tipo MIME: " + pdfTipoMime);
                System.out.println("  Tamaño: " + pdfPart.getSize() + " bytes");
                
                // Validar que sea PDF
                if (pdfTipoMime != null && pdfTipoMime.equals("application/pdf")) {
                    // Validar tamaño (5MB máximo)
                    if (pdfPart.getSize() <= 5 * 1024 * 1024) {
                        try (InputStream inputStream = pdfPart.getInputStream()) {
                            pdfContenido = inputStream.readAllBytes();
                            System.out.println("  ✅ PDF leído correctamente: " + pdfContenido.length + " bytes");
                        }
                    } else {
                        System.out.println("  ⚠️ Archivo demasiado grande: " + pdfPart.getSize() + " bytes (máximo 5MB)");
                        pdfNombre = null;
                        pdfTipoMime = null;
                    }
                } else {
                    System.out.println("  ⚠️ Tipo de archivo no válido (debe ser PDF): " + pdfTipoMime);
                    pdfNombre = null;
                    pdfTipoMime = null;
                }
                System.out.println("=============================\n");
            } else {
                System.out.println("=== NO SE ADJUNTÓ PDF ===\n");
            }
        } catch (Exception e) {
            System.out.println("  ⚠️ Error al procesar PDF: " + e.getMessage());
        }
        
        String[] miembroIds = request.getParameterValues("miembroId");
        
        System.out.println("=== PARÁMETROS PRINCIPALES ===");
        System.out.println("Comisión ID: " + comisionIdStr);
        System.out.println("Fecha reunión: " + fechaReunionStr);
        System.out.println("Observaciones: " + (observaciones != null ? observaciones : "(vacío)"));
        System.out.println("PDF adjunto: " + (pdfNombre != null ? "SÍ - " + pdfNombre : "NO"));
        System.out.println("Número de miembros: " + (miembroIds != null ? miembroIds.length : 0));
        if (miembroIds != null) {
            System.out.println("IDs de miembros: " + Arrays.toString(miembroIds));
        }
        System.out.println("==============================\n");
        
        // Validar parámetros
        if (comisionIdStr == null || fechaReunionStr == null || miembroIds == null) {
            System.out.println("❌ ERROR: Faltan parámetros obligatorios");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan datos obligatorios");
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdStr);
        
        // Buscar comisión
        Comision comision = comisionDAO.findById(comisionId);
        if (comision == null) {
            System.out.println("❌ ERROR: Comisión no encontrada con ID: " + comisionId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comisión no encontrada");
            return;
        }
        System.out.println("✅ Comisión encontrada: " + comision.getNombre());
        
        // Parsear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaReunion;
        try {
            fechaReunion = sdf.parse(fechaReunionStr);
            System.out.println("✅ Fecha parseada: " + fechaReunion);
        } catch (ParseException e) {
            System.out.println("❌ ERROR: Formato de fecha inválido: " + fechaReunionStr);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de fecha inválido");
            return;
        }
        
        // Crear acta
        Acta acta = new Acta();
        acta.setComision(comision);
        acta.setFechaReunion(fechaReunion);
        acta.setObservaciones(observaciones);
        acta.setFechaCreacion(new Date());
        
        // Añadir PDF si existe
        acta.setPdfNombre(pdfNombre);
        acta.setPdfContenido(pdfContenido);
        acta.setPdfTipoMime(pdfTipoMime);
        
        Long actaId = actaDAO.save(acta);
        
        if (actaId == null) {
            System.out.println("❌ ERROR: No se pudo guardar el acta");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al guardar el acta");
            return;
        }
        
        System.out.println("\n✅ ACTA GUARDADA CON ID: " + actaId);
        
        // Guardar asistencias
        System.out.println("\n=== GUARDANDO ASISTENCIAS ===\n");
        
        int totalProcesados = 0;
        int totalGuardados = 0;
        int totalAsistieron = 0;
        int totalNoAsistieron = 0;
        int totalConJustificacion = 0;
        
        for (String miembroIdStr : miembroIds) {
            totalProcesados++;
            Long miembroId = Long.parseLong(miembroIdStr);
            
            System.out.println("--- Miembro ID: " + miembroId + " ---");
            
            String asistenciaParam = request.getParameter("asistencia_" + miembroId);
            String justificacion = request.getParameter("justificacion_" + miembroId);
            
            System.out.println("  Radio seleccionado: " + asistenciaParam);
            System.out.println("  Justificación recibida: " + (justificacion != null ? "'" + justificacion + "'" : "null"));
            
            boolean asistio = "ASISTIO".equals(asistenciaParam);
            
            // Limpiar justificación
            if (justificacion != null) {
                justificacion = justificacion.trim();
                if (justificacion.isEmpty()) {
                    justificacion = null;
                }
            }
            
            // Si asistió, la justificación debe ser null
            if (asistio) {
                justificacion = null;
                totalAsistieron++;
                System.out.println("  ✅ ASISTIÓ - justificación = null");
            } else {
                totalNoAsistieron++;
                if (justificacion != null && !justificacion.isEmpty()) {
                    totalConJustificacion++;
                    System.out.println("  ❌ NO ASISTIÓ - con justificación: '" + justificacion + "'");
                } else {
                    System.out.println("  ❌ NO ASISTIÓ - sin justificación");
                }
            }
            
            System.out.println("  Guardando en BD:");
            System.out.println("    - acta_id: " + actaId);
            System.out.println("    - miembro_id: " + miembroId);
            System.out.println("    - asistio: " + asistio);
            System.out.println("    - justificacion: " + (justificacion != null ? "'" + justificacion + "'" : "null"));
            
            Long asistenciaId = actaDAO.saveAsistencia(actaId, miembroId, asistio, justificacion);
            
            if (asistenciaId != null) {
                totalGuardados++;
                System.out.println("  ✅ Asistencia guardada correctamente con ID: " + asistenciaId + "\n");
            } else {
                System.out.println("  ❌ Error al guardar asistencia\n");
            }
        }
        
        System.out.println("=== RESUMEN DE ASISTENCIAS ===");
        System.out.println("Total procesados: " + totalProcesados);
        System.out.println("Guardadas en BD: " + totalGuardados);
        System.out.println("Asistieron: " + totalAsistieron);
        System.out.println("No asistieron: " + totalNoAsistieron);
        System.out.println("Con justificación: " + totalConJustificacion);
        System.out.println("==============================\n");
        
        System.out.println("========================================");
        System.out.println("===> ✅ PROCESO COMPLETADO");
        System.out.println("========================================\n\n");
        
        // Redirigir a la vista del acta
        response.sendRedirect(request.getContextPath() + "/actas/view?id=" + actaId);
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
        System.out.println("Tiene PDF: " + acta.tienePdf());
        if (acta.tienePdf()) {
            System.out.println("PDF nombre: " + acta.getPdfNombre());
        }
        
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
        }
        
        // Establecer atributos
        request.setAttribute("acta", acta);
        request.setAttribute("asistencias", asistencias);
        
        // Forward a la vista
        String jspPath = "/WEB-INF/views/actas/view.jsp";
        System.out.println("\nForwarding a: " + jspPath);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
        
        System.out.println("========================================");
        System.out.println("===> VIEW ACTA COMPLETADO");
        System.out.println("========================================\n");
    }
    
    /**
     * Descarga el PDF adjunto
     */
    private void downloadPdf(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID no especificado");
            return;
        }
        
        Long actaId = Long.parseLong(idStr);
        Acta acta = actaDAO.findById(actaId);
        
        if (acta == null || !acta.tienePdf()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF no encontrado");
            return;
        }
        
        byte[] pdfContenido = actaDAO.getPdfContenido(actaId);
        
        if (pdfContenido == null || pdfContenido.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contenido del PDF no disponible");
            return;
        }
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + acta.getPdfNombre() + "\"");
        response.setContentLength(pdfContenido.length);
        
        try (OutputStream out = response.getOutputStream()) {
            out.write(pdfContenido);
            out.flush();
        }
        
        System.out.println("PDF descargado: " + acta.getPdfNombre() + " (" + pdfContenido.length + " bytes)");
    }
    
    /**
     * Visualiza el PDF en el navegador
     */
    private void viewPdf(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID no especificado");
            return;
        }
        
        Long actaId = Long.parseLong(idStr);
        Acta acta = actaDAO.findById(actaId);
        
        if (acta == null || !acta.tienePdf()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF no encontrado");
            return;
        }
        
        byte[] pdfContenido = actaDAO.getPdfContenido(actaId);
        
        if (pdfContenido == null || pdfContenido.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contenido del PDF no disponible");
            return;
        }
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + acta.getPdfNombre() + "\"");
        response.setContentLength(pdfContenido.length);
        
        try (OutputStream out = response.getOutputStream()) {
            out.write(pdfContenido);
            out.flush();
        }
        
        System.out.println("PDF visualizado: " + acta.getPdfNombre() + " (" + pdfContenido.length + " bytes)");
    }
    
    /**
     * Método auxiliar para obtener el nombre del archivo desde el Part
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
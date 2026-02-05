package com.comisiones.controller;

import com.comisiones.dao.ActaDAO;
import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model.Miembro;
import com.comisiones.util.AppConstants;
import com.comisiones.util.AppLogger;

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
    maxFileSize = (int) AppConstants.MAX_PDF_SIZE,
    maxRequestSize = (int) AppConstants.MAX_REQUEST_SIZE,
    fileSizeThreshold = (int) AppConstants.FILE_SIZE_THRESHOLD
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
        AppLogger.info("ActaController inicializado");
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
        
        AppLogger.debug("loadMiembrosComision iniciado");
        
        String comisionIdParam = request.getParameter("comisionId");
        
        if (comisionIdParam == null || comisionIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdParam);
        List<Miembro> miembros = miembroDAO.findMiembrosByComisionId(comisionId);
        
        AppLogger.debug("Miembros cargados: " + (miembros != null ? miembros.size() : 0));
        
        request.setAttribute("miembros", miembros);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/actas/miembros-fragment.jsp");
        dispatcher.forward(request, response);
    }
    
    private void saveActa(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        AppLogger.debug("Guardando acta");
        
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
                
                AppLogger.debug("Archivo PDF detectado: " + pdfNombre);
                
                // Validar que sea PDF
                if (pdfTipoMime != null && pdfTipoMime.equals(AppConstants.PDF_MIME_TYPE)) {
                    // Validar tamaño
                    if (pdfPart.getSize() <= AppConstants.MAX_PDF_SIZE) {
                        try (InputStream inputStream = pdfPart.getInputStream()) {
                            pdfContenido = inputStream.readAllBytes();
                            AppLogger.debug("PDF leído correctamente: " + pdfContenido.length + " bytes");
                        }
                    } else {
                        AppLogger.info(AppConstants.ERROR_PDF_TOO_LARGE);
                        pdfNombre = null;
                        pdfTipoMime = null;
                    }
                } else {
                    AppLogger.info(AppConstants.ERROR_INVALID_PDF);
                    pdfNombre = null;
                    pdfTipoMime = null;
                }
            }
        } catch (Exception e) {
            AppLogger.error("Error al procesar PDF", e);
        }
        
        String[] miembroIds = request.getParameterValues("miembroId");
        
        // Validar parámetros
        if (comisionIdStr == null || fechaReunionStr == null || miembroIds == null) {
            AppLogger.error(AppConstants.ERROR_MISSING_PARAMS, null);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, AppConstants.ERROR_MISSING_PARAMS);
            return;
        }
        
        Long comisionId = Long.parseLong(comisionIdStr);
        
        // Buscar comisión
        Comision comision = comisionDAO.findById(comisionId);
        if (comision == null) {
            AppLogger.error(AppConstants.ERROR_COMISION_NOT_FOUND, null);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, AppConstants.ERROR_COMISION_NOT_FOUND);
            return;
        }
        
        // Parsear fecha
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.DATE_FORMAT);
        Date fechaReunion;
        try {
            fechaReunion = sdf.parse(fechaReunionStr);
        } catch (ParseException e) {
            AppLogger.error(AppConstants.ERROR_INVALID_DATE, null);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, AppConstants.ERROR_INVALID_DATE);
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
            AppLogger.error("No se pudo guardar el acta", null);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al guardar el acta");
            return;
        }
        
        AppLogger.info("Acta guardada con ID: " + actaId);
        
        // Guardar asistencias
        AppLogger.debug("Guardando asistencias");
        
        for (String miembroIdStr : miembroIds) {
            Long miembroId = Long.parseLong(miembroIdStr);
            
            String asistenciaParam = request.getParameter("asistencia_" + miembroId);
            String justificacion = request.getParameter("justificacion_" + miembroId);
            
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
            }
            
            actaDAO.saveAsistencia(actaId, miembroId, asistio, justificacion);
        }
        
        AppLogger.info("Proceso completado correctamente");
        
        // Redirigir a la vista del acta
        response.sendRedirect(request.getContextPath() + "/actas/view?id=" + actaId);
    }
    
    private void viewActa(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        
        AppLogger.debug("View acta iniciado");
        
        String idStr = request.getParameter("id");
        
        if (idStr == null) {
            AppLogger.error("ID es null", null);
            request.setAttribute("error", "ID de acta no especificado");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        Long id = Long.parseLong(idStr);
        
        // Buscar acta
        Acta acta = actaDAO.findById(id);
        
        if (acta == null) {
            AppLogger.error("Acta no encontrada con ID " + id, null);
            request.setAttribute("error", "Acta no encontrada");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        
        AppLogger.debug("Acta encontrada: " + acta.getComision().getNombre());
        
        // Buscar asistencias
        List<AsistenciaActa> asistencias = actaDAO.findAsistenciasByActaId(id);
        
        AppLogger.debug("Asistencias encontradas: " + (asistencias != null ? asistencias.size() : 0));
        
        // Establecer atributos
        request.setAttribute("acta", acta);
        request.setAttribute("asistencias", asistencias);
        
        // Forward a la vista
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/actas/view.jsp");
        dispatcher.forward(request, response);
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
        
        response.setContentType(AppConstants.PDF_MIME_TYPE);
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
        
        response.setContentType(AppConstants.PDF_MIME_TYPE);
        response.setHeader("Content-Disposition", "inline; filename=\"" + acta.getPdfNombre() + "\"");
        response.setContentLength(pdfContenido.length);
        
        try (OutputStream out = response.getOutputStream()) {
            out.write(pdfContenido);
            out.flush();
        }
        
        AppLogger.debug("PDF visualizado: " + acta.getPdfNombre());
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
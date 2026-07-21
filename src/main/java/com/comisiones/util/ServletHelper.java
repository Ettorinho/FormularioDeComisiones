package com.comisiones.util;

import com.comisiones.model.UsuarioAD;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper para operaciones comunes en servlets
 */
public class ServletHelper {
    
    /**
     * Parsea un String a Long de forma segura
     * @param value El valor a parsear
     * @return El Long parseado o null si el valor es inválido
     */
    public static Long parseIdSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Limpiar el valor de caracteres no numéricos
            String cleaned = value.replaceAll("[^\\d]", "");
            if (cleaned.isEmpty()) {
                return null;
            }
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            AppLogger.debug("Error parseando ID: " + value + " - " + e.getMessage());
            return null;
        }
    }

    public static String getUsuarioLogueado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            UsuarioAD usuario = (UsuarioAD) session.getAttribute("usuarioLogueado");
            if (usuario != null && usuario.getUsername() != null && !usuario.getUsername().trim().isEmpty()) {
                return usuario.getUsername();
            }
        }
        return "SISTEMA";
    }

    public static Long parsePathId(HttpServletRequest request, String expectedPrefix) {
        List<String> segments = getPathSegments(request, expectedPrefix);
        if (segments.size() != 1) {
            return null;
        }
        return parseIdSafely(segments.get(0));
    }

    public static Long[] parsePathIds(HttpServletRequest request, String expectedPrefix, int expectedCount) {
        List<String> segments = getPathSegments(request, expectedPrefix);
        if (segments.size() != expectedCount) {
            return null;
        }

        Long[] ids = new Long[expectedCount];
        for (int i = 0; i < expectedCount; i++) {
            ids[i] = parseIdSafely(segments.get(i));
            if (ids[i] == null) {
                return null;
            }
        }
        return ids;
    }

    /**
     * Envía un error 400 Bad Request con un mensaje JSON
     */
    public static void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }
    
    /**
     * Envía un error 500 Internal Server Error con un mensaje JSON
     */
    public static void sendInternalError(HttpServletResponse response, String message) throws IOException {
        sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    public static void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + escapeJson(message) + "\"}");
    }

    public static void forwardError(HttpServletRequest request, HttpServletResponse response, int status, String message)
            throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("error", message);
        request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
    }
    
    /**
     * Escapa caracteres especiales en JSON
     */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static List<String> getPathSegments(HttpServletRequest request, String expectedPrefix) {
        List<String> segments = new ArrayList<>();
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || expectedPrefix == null || !pathInfo.startsWith(expectedPrefix)) {
            return segments;
        }

        String suffix = pathInfo.substring(expectedPrefix.length());
        if (suffix.isEmpty()) {
            return segments;
        }

        for (String segment : suffix.split("/")) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }
        return segments;
    }
}

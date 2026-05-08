package com.comisiones.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    
    /**
     * Envía un error 400 Bad Request con un mensaje JSON
     */
    public static void sendBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + escapeJson(message) + "\"}");
    }
    
    /**
     * Envía un error 500 Internal Server Error con un mensaje JSON
     */
    public static void sendInternalError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + escapeJson(message) + "\"}");
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
}

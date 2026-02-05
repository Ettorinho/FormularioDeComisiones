package com.comisiones.util;

public class AppConstants {
    // Constantes de archivos PDF
    public static final long MAX_PDF_SIZE = 5 * 1024 * 1024; // 5MB
    public static final long MAX_REQUEST_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long FILE_SIZE_THRESHOLD = 1024 * 1024; // 1MB
    public static final String PDF_MIME_TYPE = "application/pdf";
    
    // Constantes de formato
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    // Mensajes de error
    public static final String ERROR_PDF_TOO_LARGE = "El archivo PDF excede el tamaño máximo de 5MB";
    public static final String ERROR_INVALID_PDF = "Solo se permiten archivos PDF";
    public static final String ERROR_MISSING_PARAMS = "Faltan datos obligatorios";
    public static final String ERROR_COMISION_NOT_FOUND = "Comisión no encontrada";
    public static final String ERROR_INVALID_DATE = "Formato de fecha inválido";
    
    private AppConstants() {
        // Clase de utilidad, no instanciable
    }
}

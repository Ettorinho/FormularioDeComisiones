package com.comisiones.util;

/**
 * Utilidad compartida para detectar rutas públicas y recursos estáticos.
 * Centraliza la lógica duplicada de AuthFilter, RolFilter y CsrfFilter.
 */
public final class StaticResourceUtil {

    private StaticResourceUtil() {
        // Utility class; do not instantiate
    }

    /**
     * Determina si la ruta es una ruta pública que no requiere autenticación ni autorización:
     * /login, /logout y todos los recursos estáticos.
     *
     * @param path ruta relativa al contexto de la aplicación (sin context path)
     * @return true si la ruta es pública o es un recurso estático
     */
    public static boolean esRutaPublica(String path) {
        if ("/login".equals(path) || "/logout".equals(path)) {
            return true;
        }
        return esRecursoEstatico(path);
    }

    /**
     * Determina si la ruta apunta a un recurso estático (CSS, JS, imágenes, fuentes, etc.)
     * basándose en el prefijo de ruta o la extensión de archivo.
     *
     * @param path ruta relativa al contexto de la aplicación (sin context path)
     * @return true si la ruta apunta a un recurso estático
     */
    public static boolean esRecursoEstatico(String path) {
        if (path.startsWith("/css/")       || path.startsWith("/js/")
                || path.startsWith("/img/")    || path.startsWith("/images/")
                || path.startsWith("/fonts/")  || path.startsWith("/webjars/")
                || path.startsWith("/resources/")) {
            return true;
        }
        String pathLower = path.toLowerCase();
        return pathLower.endsWith(".css")   || pathLower.endsWith(".js")
                || pathLower.endsWith(".png")   || pathLower.endsWith(".jpg")
                || pathLower.endsWith(".jpeg")  || pathLower.endsWith(".ico")
                || pathLower.endsWith(".svg")   || pathLower.endsWith(".woff")
                || pathLower.endsWith(".woff2");
    }
}

package com.comisiones.util;

public class AppLogger {
    private static final boolean DEBUG_MODE = false; // Cambiar a true para debugging
    
    public static void debug(String message) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }
    
    public static void error(String message, Exception e) {
        System.err.println("[ERROR] " + message);
        if (e != null && DEBUG_MODE) {
            e.printStackTrace();
        }
    }
    
    public static void separator() {
        if (DEBUG_MODE) {
            System.out.println("========================================");
        }
    }
}

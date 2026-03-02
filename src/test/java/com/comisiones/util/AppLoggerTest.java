package com.comisiones.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test básico para validar que la infraestructura de testing funciona correctamente.
 */
public class AppLoggerTest {

    @Test
    public void testAppLoggerNotNull() {
        assertNotNull(AppLogger.class);
    }

    @Test
    public void testDebugMethodExists() {
        assertDoesNotThrow(() -> AppLogger.debug("Test message"));
    }

    @Test
    public void testInfoMethodExists() {
        assertDoesNotThrow(() -> AppLogger.info("Test message"));
    }

    @Test
    public void testErrorMethodExists() {
        assertDoesNotThrow(() -> AppLogger.error("Test message", null));
    }
}

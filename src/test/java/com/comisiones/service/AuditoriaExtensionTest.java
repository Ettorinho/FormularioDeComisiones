package com.comisiones.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AuditoriaExtensionTest {

    @Test
    void migrationAddsSecurityAndPerformanceFieldsAndViews() throws IOException {
        String migration = Files.readString(Path.of("database/migrations/006_extender_auditoria_acciones.sql"));
        assertTrue(migration.contains("ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500)"));
        assertTrue(migration.contains("ADD COLUMN IF NOT EXISTS resultado VARCHAR(20)"));
        assertTrue(migration.contains("ADD COLUMN IF NOT EXISTS duracion_ms INTEGER"));
        assertTrue(migration.contains("ADD COLUMN IF NOT EXISTS mensaje_error TEXT"));
        assertTrue(migration.contains("ADD COLUMN IF NOT EXISTS sesion_id VARCHAR(100)"));
        assertTrue(migration.contains("CREATE OR REPLACE VIEW vw_auditoria_fuerza_bruta"));
        assertTrue(migration.contains("CREATE OR REPLACE VIEW vw_auditoria_actividad_sospechosa"));
        assertTrue(migration.contains("CREATE OR REPLACE VIEW vw_auditoria_operaciones_lentas"));
    }

    @Test
    void auditoriaServiceExposesOutcomeSpecificRegistrationMethods() throws IOException {
        String source = Files.readString(Path.of("src/main/java/com/comisiones/service/AuditoriaService.java"));
        assertTrue(source.contains("public void registrarExito("));
        assertTrue(source.contains("public void registrarFallo("));
        assertTrue(source.contains("public void registrarDenegado("));
        assertTrue(source.contains("resultado, duracion_ms, mensaje_error, sesion_id"));
    }
}

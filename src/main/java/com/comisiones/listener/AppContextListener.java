package com.comisiones.listener;

import com.comisiones.util.AppLogger;
import com.comisiones.util.DBUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppLogger.info("Aplicación iniciada — contexto inicializado");
        runStartupChecks();
    }

    /**
     * Valida la compatibilidad DB↔Java al arranque (fail-fast).
     * Comprueba que las migraciones críticas hayan sido aplicadas antes
     * de que la aplicación empiece a aceptar tráfico.
     */
    private void runStartupChecks() {
        AppLogger.info("[StartupCheck] Verificando compatibilidad DB...");
        List<String> errors = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {

            // Comprobación 1: columna actas.titulo (requiere migración V14)
            if (!columnExists(conn, "actas", "titulo")) {
                errors.add("Falta la columna 'actas.titulo'. Aplique la migración V14__add_titulo_to_actas.sql");
            }

            // Comprobación 2: valor MIXTA en enum area_type (requiere migración V8)
            if (!enumValueExists(conn, "area_type", "MIXTA")) {
                errors.add("Falta el valor 'MIXTA' en el tipo 'area_type'. Aplique la migración V8__agregar_area_mixta.sql");
            }

        } catch (SQLException e) {
            AppLogger.error("[StartupCheck] No se pudo conectar a la BD para las comprobaciones de arranque", e);
            throw new RuntimeException("[StartupCheck] Error al verificar compatibilidad DB: " + e.getMessage(), e);
        }

        if (!errors.isEmpty()) {
            StringBuilder msg = new StringBuilder("[StartupCheck] La base de datos no está al día con el código. Migraciones pendientes:\n");
            for (String err : errors) {
                msg.append("  - ").append(err).append("\n");
            }
            msg.append("Consulte database/migrations/README.md para instrucciones de migración.");
            AppLogger.error(msg.toString(), null);
            throw new RuntimeException(msg.toString());
        }

        AppLogger.info("[StartupCheck] Compatibilidad DB verificada correctamente.");
    }

    /**
     * Comprueba si existe una columna en una tabla del esquema público.
     */
    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns "
                   + "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Comprueba si existe un valor concreto en un tipo ENUM de PostgreSQL.
     */
    private boolean enumValueExists(Connection conn, String typeName, String enumValue) throws SQLException {
        String sql = "SELECT 1 FROM pg_enum e "
                   + "JOIN pg_type t ON e.enumtypid = t.oid "
                   + "WHERE t.typname = ? AND e.enumlabel = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, typeName);
            stmt.setString(2, enumValue);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AppLogger.info("Aplicación detenida — cerrando connection pool...");
        DBUtil.close();
    }
}

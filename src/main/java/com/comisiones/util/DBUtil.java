package com.comisiones.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static HikariDataSource dataSource;

    static {
        try {
            // Leer credenciales desde variables de entorno con valores por defecto seguros
            String url = System.getenv("DB_URL");
            if (url == null || url.trim().isEmpty()) {
                url = "jdbc:postgresql://localhost:5432/comisiones";
            }
            
            String usuario = System.getenv("DB_USER");
            if (usuario == null || usuario.trim().isEmpty()) {
                usuario = "postgres";
            }
            
            String contrasena = System.getenv("DB_PASSWORD");
            if (contrasena == null || contrasena.trim().isEmpty()) {
                contrasena = "changeme";
            }

            // Configurar HikariCP para connection pooling
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(usuario);
            config.setPassword(contrasena);
            
            // Configuración del pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30 segundos
            config.setIdleTimeout(600000); // 10 minutos
            config.setMaxLifetime(1800000); // 30 minutos
            
            // Configuración adicional de PostgreSQL
            config.setDriverClassName("org.postgresql.Driver");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            
            AppLogger.info("Connection pool inicializado correctamente");
        } catch (Exception e) {
            AppLogger.error("Error al inicializar el connection pool", e);
            throw new RuntimeException("No se pudo inicializar el connection pool", e);
        }
    }

    /**
     * Obtiene una conexión a la base de datos desde el pool.
     * @return un objeto Connection.
     * @throws SQLException si ocurre un error al conectar.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El connection pool no está inicializado");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Cierra el connection pool. Debe llamarse al cerrar la aplicación.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            AppLogger.info("Connection pool cerrado");
        }
    }
}
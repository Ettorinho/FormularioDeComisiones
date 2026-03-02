package com.comisiones.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static HikariDataSource dataSource;

    static {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            String url       = (String) envCtx.lookup("db/url");
            String usuario   = (String) envCtx.lookup("db/username");
            String contrasena = (String) envCtx.lookup("db/password");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(usuario);
            config.setPassword(contrasena);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setDriverClassName("org.postgresql.Driver");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            AppLogger.info("Connection pool HikariCP inicializado correctamente (JNDI/context.xml)");

        } catch (NamingException e) {
            AppLogger.error("Error JNDI: no se pudieron leer las credenciales de BD desde context.xml", e);
            throw new RuntimeException("No se pudo leer configuracion JNDI de la BD", e);
        } catch (Exception e) {
            AppLogger.error("Error al inicializar el connection pool", e);
            throw new RuntimeException("No se pudo inicializar el connection pool", e);
        }
    }

    /**
     * Obtiene una conexion a la base de datos desde el pool HikariCP.
     * Las credenciales se leen desde JNDI (context.xml, excluido del repo):
     * - java:comp/env/db/url
     * - java:comp/env/db/username
     * - java:comp/env/db/password
     *
     * @return un objeto Connection.
     * @throws SQLException si ocurre un error al conectar.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El connection pool no esta inicializado");
        }
        return dataSource.getConnection();
    }

    /**
     * Cierra el connection pool HikariCP.
     * Debe llamarse al cerrar la aplicacion (p.ej. desde un ServletContextListener).
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            AppLogger.info("Connection pool HikariCP cerrado");
        }
    }
}

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
            // Leer credenciales desde JNDI (definidas en context.xml, excluido del repo)
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");

            String url = (String) envCtx.lookup("db/url");
            String usuario = (String) envCtx.lookup("db/username");
            String contrasena = (String) envCtx.lookup("db/password");

            if (url == null || url.trim().isEmpty()) {
                throw new IllegalStateException(
                    "El parámetro JNDI 'db/url' no está configurado en context.xml"
                );
            }
            if (usuario == null || usuario.trim().isEmpty()) {
                throw new IllegalStateException(
                    "El parámetro JNDI 'db/username' no está configurado en context.xml"
                );
            }
            if (contrasena == null || contrasena.trim().isEmpty()) {
                throw new IllegalStateException(
                    "El parámetro JNDI 'db/password' no está configurado en context.xml"
                );
            }

            // Configurar HikariCP para connection pooling
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(usuario);
            config.setPassword(contrasena);

            // Configuración del pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);   // 30 segundos
            config.setIdleTimeout(600000);         // 10 minutos
            config.setMaxLifetime(1800000);        // 30 minutos

            // Configuración adicional de PostgreSQL
            config.setDriverClassName("org.postgresql.Driver");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            AppLogger.info("Connection pool HikariCP inicializado correctamente (credenciales desde JNDI/context.xml)");
        } catch (NamingException e) {
            AppLogger.error("Error al leer credenciales JNDI desde context.xml. " +
                "Asegúrate de que context.xml contiene los parámetros db/url, db/username y db/password.", e);
            throw new RuntimeException("No se pudo leer la configuración JNDI de la BD", e);
        } catch (Exception e) {
            AppLogger.error("Error al inicializar el connection pool", e);
            throw new RuntimeException("No se pudo inicializar el connection pool", e);
        }
    }

    /**
     * Obtiene una conexión a la base de datos desde el pool HikariCP.
     * Las credenciales se leen desde JNDI, configuradas en {@code context.xml}
     * (excluido del repositorio mediante .gitignore):
     * <ul>
     *   <li>{@code java:comp/env/db/url}      – URL JDBC de conexión</li>
     *   <li>{@code java:comp/env/db/username} – Usuario de la base de datos</li>
     *   <li>{@code java:comp/env/db/password} – Contraseña de la base de datos</li>
     * </ul>
     *
     * Ejemplo de configuración en {@code src/main/webapp/META-INF/context.xml}:
     * <pre>
     * &lt;Environment name="db/url"      type="java.lang.String" value="jdbc:postgresql://host:5432/bd"/&gt;
     * &lt;Environment name="db/username" type="java.lang.String" value="usuario"/&gt;
     * &lt;Environment name="db/password" type="java.lang.String" value="contraseña"/&gt;
     * </pre>
     *
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
     * Cierra el connection pool HikariCP.
     * Debe llamarse al cerrar la aplicación (p.ej. desde un ServletContextListener).
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            AppLogger.info("Connection pool HikariCP cerrado");
        }
    }
}
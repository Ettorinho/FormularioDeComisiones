package com.comisiones.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {

    private static final String JNDI_NAME = "java:comp/env/jdbc/Comisiones";

    private static volatile DataSource dataSource;

    private static DataSource getDataSource() throws NamingException {
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    Context initCtx = new InitialContext();
                    dataSource = (DataSource) initCtx.lookup(JNDI_NAME);
                    AppLogger.info("DataSource JNDI obtenido: " + JNDI_NAME);
                }
            }
        }
        return dataSource;
    }

    /**
     * Obtiene una conexión a la base de datos desde el DataSource JNDI
     * {@code jdbc/Comisiones} configurado en {@code context.xml}.
     *
     * @return un objeto Connection.
     * @throws SQLException si ocurre un error al obtener la conexión.
     */
    public static Connection getConnection() throws SQLException {
        try {
            return getDataSource().getConnection();
        } catch (NamingException e) {
            AppLogger.error("Error al obtener DataSource JNDI: " + JNDI_NAME, e);
            throw new SQLException("No se pudo obtener el DataSource JNDI: " + JNDI_NAME, e);
        }
    }

    /**
     * Método de cierre mantenido por compatibilidad.
     * El ciclo de vida del pool es gestionado por el contenedor (Tomcat).
     */
    public static void close() {
        // El pool JNDI es gestionado por Tomcat; no se requiere cierre manual.
    }
}
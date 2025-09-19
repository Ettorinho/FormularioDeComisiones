package com.comisiones.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // --- IMPORTANTE ---
    // URL de conexión a tu base de datos PostgreSQL.
    private static final String URL = "jdbc:postgresql://localhost:5432/comisiones";
    
    // Usuario de la base de datos.
    private static final String USUARIO = "postgres";
    
    // --- ¡ACCIÓN REQUERIDA! ---
    // Reemplaza "TU_CONTRASENA_AQUI" con la contraseña real de tu usuario 'postgres'.
    private static final String CONTRASENA = "Master03.";

    /**
     * Obtiene una conexión a la base de datos.
     * @return un objeto Connection.
     * @throws SQLException si ocurre un error al conectar.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Asegura que el driver de PostgreSQL esté cargado.
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // Este error es crítico y significa que el JAR del driver no está en el proyecto.
            throw new SQLException("Error: No se encontró el driver de PostgreSQL. Asegúrate de que la dependencia de Maven esté correcta.", e);
        }
        // Intenta establecer la conexión con las credenciales proporcionadas.
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
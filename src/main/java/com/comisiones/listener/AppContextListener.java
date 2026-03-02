package com.comisiones.listener;

import com.comisiones.util.AppLogger;
import com.comisiones.util.DBUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppLogger.info("Aplicación iniciada — contexto inicializado");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AppLogger.info("Aplicación detenida — cerrando connection pool...");
        DBUtil.close();
    }
}

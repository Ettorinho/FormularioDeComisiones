package com.comisiones.controller;

import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// La anotación define la URL para este servlet.
@WebServlet("/miembros")
public class MiembroController extends HttpServlet {

    private MiembroDAO miembroDAO;

    @Override
    public void init() {
        miembroDAO = new MiembroDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Obtener la lista completa de miembros desde el DAO.
            List<Miembro> listaMiembros = miembroDAO.findAll();

            // 2. Guardar la lista en el objeto 'request' para que la vista pueda usarla.
            //    El nombre "miembros" es el que usaremos en el JSP.
            request.setAttribute("miembros", listaMiembros);

            // 3. Redirigir la petición a la vista JSP correcta.
            //    Esta es la ruta que antes daba error 404.
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/miembros/list.jsp");
            dispatcher.forward(request, response);

        } catch (SQLException e) {
            // Si hay un error de base de datos, lo mostramos en la página de error.
            e.printStackTrace();
            request.setAttribute("error", "Error al cargar miembros: " + e.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
            dispatcher.forward(request, response);
        }
    }
}
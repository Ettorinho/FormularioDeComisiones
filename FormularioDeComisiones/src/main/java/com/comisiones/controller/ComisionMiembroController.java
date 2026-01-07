package com.comisiones.controller;

import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/comisiones/addMember/*")
public class ComisionMiembroController extends HttpServlet {

    private MiembroDAO miembroDAO;
    private ComisionMiembroDAO comisionMiembroDAO;
    private ComisionDAO comisionDAO; // Necesitamos este DAO para obtener el objeto Comision

    @Override
    public void init() {
        miembroDAO = new MiembroDAO();
        comisionMiembroDAO = new ComisionMiembroDAO();
        comisionDAO = new ComisionDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            Long comisionId = Long.parseLong(pathInfo.substring(1));

            String nombreApellidos = request.getParameter("nombreApellidos");
            String dni = request.getParameter("dni");
            String email = request.getParameter("email");
            String cargoStr = request.getParameter("cargo");
            String fechaIncorporacionStr = request.getParameter("fechaIncorporacion");

            Miembro nuevoMiembro = new Miembro();
            nuevoMiembro.setNombreApellidos(nombreApellidos);
            nuevoMiembro.setDniNif(dni);
            nuevoMiembro.setEmail(email);
            
            // CORRECCIÓN: El método save() es void. Lo llamamos y luego obtenemos el ID del objeto.
            miembroDAO.save(nuevoMiembro);
            // Ahora nuevoMiembro tiene el ID que le asignó la base de datos.

            Comision comision = comisionDAO.findById(comisionId);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaIncorporacion = formatter.parse(fechaIncorporacionStr);

            ComisionMiembro comisionMiembro = new ComisionMiembro();
            
            // CORRECCIÓN: Usar los setters de objeto y el enum
            comisionMiembro.setComision(comision);
            comisionMiembro.setMiembro(nuevoMiembro);
            comisionMiembro.setCargo(ComisionMiembro.Cargo.valueOf(cargoStr.toUpperCase()));
            comisionMiembro.setFechaIncorporacion(new java.sql.Date(fechaIncorporacion.getTime()));

            comisionMiembroDAO.save(comisionMiembro);

            response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);

        } catch (SQLException | ParseException | IllegalArgumentException e) {
            // IllegalArgumentException es por si el valor del 'cargo' no es válido
            throw new ServletException("Error al guardar el miembro de la comisión", e);
        }
    }
}
package com.comisiones.controller;

import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
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

// Este controlador solo se encarga de añadir miembros a una comisión
@WebServlet("/addMember/*")
public class AddMemberController extends HttpServlet {

    private MiembroDAO miembroDAO;
    private ComisionMiembroDAO comisionMiembroDAO;
    private ComisionDAO comisionDAO;

    @Override
    public void init() {
        miembroDAO = new MiembroDAO();
        comisionMiembroDAO = new ComisionMiembroDAO();
        comisionDAO = new ComisionDAO();
    }

    /**
     * Muestra el formulario para añadir un nuevo miembro a una comisión.
     * Se activa con GET /addMember/{comisionId}
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long comisionId = Long.parseLong(request.getPathInfo().substring(1));
            Comision comision = comisionDAO.findById(comisionId);

            request.setAttribute("comision", comision);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/addMember.jsp");
            dispatcher.forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Error al cargar el formulario para añadir miembro", e);
        }
    }

    /**
     * Guarda el nuevo miembro y su relación con la comisión.
     * Se activa con POST /addMember/{comisionId}
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long comisionId = Long.parseLong(request.getPathInfo().substring(1));

            String nombreApellidos = request.getParameter("nombreApellidos");
            String dni = request.getParameter("dni");
            String email = request.getParameter("email");
            String cargoStr = request.getParameter("cargo");
            String fechaIncorporacionStr = request.getParameter("fechaIncorporacion");

            Miembro nuevoMiembro = new Miembro();
            nuevoMiembro.setNombreApellidos(nombreApellidos);
            nuevoMiembro.setDniNif(dni);
            nuevoMiembro.setEmail(email);
            
            miembroDAO.save(nuevoMiembro); // Guardamos y el ID se actualiza en el objeto

            Comision comision = comisionDAO.findById(comisionId);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaIncorporacion = formatter.parse(fechaIncorporacionStr);

            ComisionMiembro comisionMiembro = new ComisionMiembro();
            comisionMiembro.setComision(comision);
            comisionMiembro.setMiembro(nuevoMiembro);
            comisionMiembro.setCargo(ComisionMiembro.Cargo.valueOf(cargoStr.toUpperCase()));
            comisionMiembro.setFechaIncorporacion(new java.sql.Date(fechaIncorporacion.getTime()));

            comisionMiembroDAO.save(comisionMiembro);

            response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);

        } catch (SQLException | ParseException | IllegalArgumentException e) {
            throw new ServletException("Error al guardar el miembro de la comisión", e);
        }
    }
}
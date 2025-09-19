package com.comisiones.controller;

// ¡IMPORTANTE! Se ha eliminado la anotación @WebServlet de aquí.

import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
// No importamos WebServlet
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ComisionController extends HttpServlet {

    private ComisionDAO comisionDAO;
    private ComisionMiembroDAO comisionMiembroDAO;
    private MiembroDAO miembroDAO;

    @Override
    public void init() {
        comisionDAO = new ComisionDAO();
        comisionMiembroDAO = new ComisionMiembroDAO();
        miembroDAO = new MiembroDAO();
        System.out.println("--- ComisionController INICIALIZADO ---");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n--- ENTRANDO A doPost DE ComisionController ---");
        String pathInfo = request.getPathInfo();
        System.out.println("doPost -> PathInfo recibido: " + pathInfo);
        
        try {
            if (pathInfo != null && pathInfo.startsWith("/addMember")) {
                System.out.println("doPost -> Decisión: Llamar a saveMemberInComision");
                saveMemberInComision(request, response);
            } else {
                System.out.println("doPost -> Decisión: Llamar a saveComision");
                saveComision(request, response);
            }
        } catch (SQLException | ParseException e) {
            throw new ServletException("Error en la operación POST", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n--- ENTRANDO A doGet DE ComisionController ---");
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();

        System.out.println("doGet -> RequestURI: " + requestURI);
        System.out.println("doGet -> ServletPath: " + servletPath);
        System.out.println("doGet -> PathInfo: " + pathInfo);

        if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/list";
            System.out.println("doGet -> PathInfo normalizado a: /list");
        }

        try {
            if (pathInfo.startsWith("/view/")) {
                System.out.println("doGet -> Decisión: Llamar a viewComision");
                viewComision(request, response);
            } else if (pathInfo.startsWith("/addMember/")) {
                System.out.println("doGet -> Decisión: Llamar a showAddMemberForm");
                showAddMemberForm(request, response);
            } else {
                System.out.println("doGet -> Decisión: Entrar al switch");
                switch (pathInfo) {
                    case "/new":
                        System.out.println("doGet -> switch: Llamar a showNewForm");
                        showNewForm(request, response);
                        break;
                    case "/list":
                    default:
                        System.out.println("doGet -> switch: Llamar a listComisiones");
                        listComisiones(request, response);
                        break;
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Error en la operación GET", e);
        }
    }

    // ... (El resto de los métodos: listComisiones, showNewForm, etc. se quedan IGUAL)
    // ... (No es necesario copiar el resto, ya que no cambian)
    private void listComisiones(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Comision> listaComisiones = comisionDAO.findAll();
        request.setAttribute("comisiones", listaComisiones);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/list.jsp");
        dispatcher.forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/form.jsp");
        dispatcher.forward(request, response);
    }

    private void viewComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Long id = Long.parseLong(request.getPathInfo().substring(6));
        Comision comision = comisionDAO.findById(id);
        List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(id);
        request.setAttribute("comision", comision);
        request.setAttribute("miembros", miembros);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/view.jsp");
        dispatcher.forward(request, response);
    }

    private void showAddMemberForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Long comisionId = Long.parseLong(request.getPathInfo().substring(11));
        Comision comision = comisionDAO.findById(comisionId);
        request.setAttribute("comision", comision);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/addMember.jsp");
        dispatcher.forward(request, response);
    }

    private void saveComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ParseException, IOException {
        String nombre = request.getParameter("nombre");
        String fechaConstitucionStr = request.getParameter("fechaConstitucion");
        Comision nuevaComision = new Comision();
        nuevaComision.setNombre(nombre);
        Date fechaConst = new SimpleDateFormat("yyyy-MM-dd").parse(fechaConstitucionStr);
        nuevaComision.setFechaConstitucion(new java.sql.Date(fechaConst.getTime()));
        comisionDAO.save(nuevaComision);
        response.sendRedirect(request.getContextPath() + "/comisiones/list");
    }

    private void saveMemberInComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ParseException, IOException {
        Long comisionId = Long.parseLong(request.getPathInfo().substring(11));
        String nombreApellidos = request.getParameter("nombreApellidos");
        String dni = request.getParameter("dni");
        String email = request.getParameter("email");
        String cargoStr = request.getParameter("cargo");
        String fechaIncorporacionStr = request.getParameter("fechaIncorporacion");
        Miembro nuevoMiembro = new Miembro();
        nuevoMiembro.setNombreApellidos(nombreApellidos);
        nuevoMiembro.setDniNif(dni);
        nuevoMiembro.setEmail(email);
        miembroDAO.save(nuevoMiembro);
        Comision comision = comisionDAO.findById(comisionId);
        Date fechaIncorp = new SimpleDateFormat("yyyy-MM-dd").parse(fechaIncorporacionStr);
        ComisionMiembro comisionMiembro = new ComisionMiembro();
        comisionMiembro.setComision(comision);
        comisionMiembro.setMiembro(nuevoMiembro);
        comisionMiembro.setCargo(ComisionMiembro.Cargo.valueOf(cargoStr.toUpperCase()));
        comisionMiembro.setFechaIncorporacion(new java.sql.Date(fechaIncorp.getTime()));
        comisionMiembroDAO.save(comisionMiembro);
        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }
}
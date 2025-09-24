package com.comisiones.controller;

import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/list";
        }
        try {
            if (pathInfo.equals("/buscarPorDni")) {
                showBuscarPorDniForm(request, response);
            } else if (pathInfo.equals("/buscarComision")) {
                showBuscarComisionForm(request, response);
            } else if (pathInfo.startsWith("/view/")) {
                viewComision(request, response);
            } else if (pathInfo.startsWith("/addMember/")) {
                showAddMemberForm(request, response);
            } else if (pathInfo.startsWith("/bajaMiembros/")) {
                showBajaMiembrosForm(request, response);
            } else {
                switch (pathInfo) {
                    case "/new":
                        showNewForm(request, response);
                        break;
                    case "/list":
                    default:
                        listComisiones(request, response);
                        break;
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Error en la operación GET", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.equals("/buscarPorDni")) {
                buscarComisionesPorDni(request, response);
            } else if (pathInfo != null && pathInfo.equals("/buscarComision")) {
                buscarComisionPorNombre(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/addMember/")) {
                saveMemberInComision(request, response);
            } else if (pathInfo != null && pathInfo.startsWith("/bajaMiembro/")) {
                bajaMiembro(request, response);
            } else {
                saveComision(request, response);
            }
        } catch (SQLException | ParseException e) {
            throw new ServletException("Error en la operación POST", e);
        }
    }

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
        String pathInfo = request.getPathInfo();
        String idStr = pathInfo.substring(6);

        // Log para depuración
        System.out.println("DEBUG: pathInfo = " + pathInfo);
        System.out.println("DEBUG: id a parsear = '" + idStr + "'");

        Long id;
        try {
            // Elimina cualquier carácter no numérico (opcional, si solo aceptas números)
            id = Long.parseLong(idStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            // Opcional: muestra un error amigable
            request.setAttribute("error", "ID de comisión no válido en la URL: " + idStr);
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

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

    private void showBajaMiembrosForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Long comisionId = Long.parseLong(request.getPathInfo().substring(14));
        Comision comision = comisionDAO.findById(comisionId);
        List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(comisionId);
        // Filtra solo miembros activos
        List<ComisionMiembro> miembrosActivos = new ArrayList<>();
        for (ComisionMiembro cm : miembros) {
            if (cm.getFechaBaja() == null) {
                miembrosActivos.add(cm);
            }
        }
        request.setAttribute("comision", comision);
        request.setAttribute("miembros", miembrosActivos);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/bajaMiembros.jsp");
        dispatcher.forward(request, response);
    }

    private void saveComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ParseException, IOException {
        System.out.println("===> [DEBUG] saveComision INVOCADO");
        String nombre = request.getParameter("nombre");
        String fechaConstitucionStr = request.getParameter("fechaConstitucion");
        String fechaFinStr = request.getParameter("fechaFin");

        Comision nuevaComision = new Comision();
        nuevaComision.setNombre(nombre);
        Date fechaConst = new SimpleDateFormat("yyyy-MM-dd").parse(fechaConstitucionStr);
        nuevaComision.setFechaConstitucion(new java.sql.Date(fechaConst.getTime()));

        if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
            Date fechaFin = new SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
            nuevaComision.setFechaFin(new java.sql.Date(fechaFin.getTime()));
        }

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

    // --- Buscador por DNI ---
    private void showBuscarPorDniForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarPorDni.jsp");
        dispatcher.forward(request, response);
    }

    private void buscarComisionesPorDni(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        String dni = request.getParameter("dni");
        Miembro miembro = miembroDAO.findByDni(dni);
        List<ComisionMiembro> comisiones = null;
        if (miembro != null) {
            comisiones = comisionMiembroDAO.findByMiembroId(miembro.getId());
        }
        request.setAttribute("miembro", miembro);
        request.setAttribute("comisiones", comisiones);
        request.setAttribute("dniBuscado", dni);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarPorDni.jsp");
        dispatcher.forward(request, response);
    }

    // --- Buscador de Comisión por nombre (y muestra miembros) ---
    private void showBuscarComisionForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarComision.jsp");
        dispatcher.forward(request, response);
    }

    private void buscarComisionPorNombre(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        String nombre = request.getParameter("nombre");
        List<Comision> comisiones = comisionDAO.findByNombreLike(nombre);
        // Para cada comisión, obtener los miembros
        for (Comision comision : comisiones) {
            List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(comision.getId());
            comision.setMiembros(new HashSet<>(miembros));
        }
        request.setAttribute("comisiones", comisiones);
        request.setAttribute("nombreBuscado", nombre);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarComision.jsp");
        dispatcher.forward(request, response);
    }

    // --- Baja lógica de miembro ---
   private void bajaMiembro(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ParseException {
    String[] parts = request.getPathInfo().split("/");
    Long comisionId = Long.parseLong(parts[2]);
    Long miembroId = Long.parseLong(parts[3]);
    String fechaBajaStr = request.getParameter("fechaBaja");
    System.out.println("[bajaMiembro] comisionId=" + comisionId + ", miembroId=" + miembroId + ", fechaBajaStr=" + fechaBajaStr);
    Date fechaBajaUtil = new SimpleDateFormat("yyyy-MM-dd").parse(fechaBajaStr);
    java.sql.Date fechaBaja = new java.sql.Date(fechaBajaUtil.getTime());
    comisionMiembroDAO.darDeBaja(comisionId, miembroId, fechaBaja);
    System.out.println("[bajaMiembro] Redirecting to: " + request.getContextPath() + "/comisiones/view/" + comisionId);
    response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
 }
}
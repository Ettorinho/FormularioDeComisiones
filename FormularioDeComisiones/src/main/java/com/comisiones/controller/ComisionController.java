package com.comisiones.controller;

import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model. Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet. http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java. text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util. ArrayList;

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
            } else if (pathInfo.equals("/existentes")) {
                getComisionesExistentes(request, response);
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
            } else if (pathInfo != null && pathInfo. equals("/buscarComision")) {
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

        System.out.println("DEBUG: pathInfo = " + pathInfo);
        System.out.println("DEBUG: id a parsear = '" + idStr + "'");

        Long id;
        try {
            id = Long.parseLong(idStr. replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            request.setAttribute("error", "ID de comisión no válido en la URL:  " + idStr);
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

    private void saveComision(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ParseException, IOException, ServletException {
        System.out.println("===> [DEBUG] saveComision INVOCADO");
        
        String nombre = request.getParameter("nombre");
        String areaStr = request.getParameter("area");
        String tipoStr = request.getParameter("tipo");
        String fechaConstitucionStr = request.getParameter("fechaConstitucion");
        String fechaFinStr = request. getParameter("fechaFin");
        String miembrosJSON = request.getParameter("miembrosJSON");
        String opcionCreacion = request.getParameter("opcionCreacion");
        String comisionExistenteStr = request.getParameter("comisionExistente");
        
        System.out.println("===> Parámetros recibidos:");
        System.out.println("    nombre:  " + nombre);
        System.out.println("    area: " + areaStr);
        System.out.println("    tipo: " + tipoStr);
        System.out.println("    fechaConstitucion: " + fechaConstitucionStr);
        System.out.println("    opcionCreacion: " + opcionCreacion);
        System.out.println("    comisionExistente: " + comisionExistenteStr);
        System.out.println("    miembrosJSON: " + miembrosJSON);
        
        // Validar área y tipo
        if (areaStr == null || tipoStr == null) {
            request.setAttribute("error", "Debe seleccionar área y tipo");
            showNewForm(request, response);
            return;
        }
        
        Long comisionId = null;
        
        // Opción 1: Agregar miembros a comisión existente
        if ("existente".equals(opcionCreacion) && comisionExistenteStr != null && !comisionExistenteStr.isEmpty()) {
            comisionId = Long.parseLong(comisionExistenteStr);
            System.out.println("===> Agregando miembros a comisión existente ID: " + comisionId);
        } 
        // Opción 2: Crear nueva comisión
        else {
            if (nombre == null || nombre.trim().isEmpty() || fechaConstitucionStr == null) {
                request.setAttribute("error", "Debe proporcionar nombre y fecha de constitución");
                showNewForm(request, response);
                return;
            }
            
            Comision nuevaComision = new Comision();
            nuevaComision. setNombre(nombre. trim());
            nuevaComision.setArea(Comision.Area.valueOf(areaStr));
            nuevaComision.setTipo(Comision.Tipo.valueOf(tipoStr));

            Date fechaConst = new SimpleDateFormat("yyyy-MM-dd").parse(fechaConstitucionStr);
            nuevaComision.setFechaConstitucion(new java.sql.Date(fechaConst.getTime()));

            if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
                Date fechaFin = new SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
                nuevaComision. setFechaFin(new java.sql.Date(fechaFin.getTime()));
            }
            
            // Verificar si ya existe
            if (comisionDAO.exists(nombre. trim(), nuevaComision.getArea(), nuevaComision.getTipo())) {
                request. setAttribute("error", "Ya existe una " + nuevaComision.getTipo().getDescripcion() + 
                                              " con ese nombre en " + nuevaComision.getArea().getDescripcion());
                showNewForm(request, response);
                return;
            }
            
            comisionDAO.save(nuevaComision);
            comisionId = nuevaComision.getId();
            System.out.println("===> Nueva comisión creada con ID: " + comisionId);
        }
        
        // Procesar miembros desde JSON
        if (miembrosJSON != null && !miembrosJSON.isEmpty() && ! miembrosJSON.equals("[]")) {
            System.out. println("===> Procesando miembros.. .");
            procesarMiembrosJSON(comisionId, miembrosJSON);
        } else {
            System.out. println("===> No hay miembros para procesar");
        }

        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }

    private void saveMemberInComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ParseException, IOException {
        Long comisionId = Long.parseLong(request.getPathInfo().substring(11));
        String nombreApellidos = request.getParameter("nombreApellidos");
        String dni = request.getParameter("dni");
        String email = request.getParameter("email");
        String cargoStr = request.getParameter("cargo");
        String fechaIncorporacionStr = request. getParameter("fechaIncorporacion");

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
        comisionMiembro.setCargo(ComisionMiembro.Cargo. valueOf(cargoStr. toUpperCase()));
        comisionMiembro.setFechaIncorporacion(new java.sql.Date(fechaIncorp.getTime()));

        comisionMiembroDAO. save(comisionMiembro);
        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }

    private void showBuscarPorDniForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarPorDni.jsp");
        dispatcher.forward(request, response);
    }

    private void buscarComisionesPorDni(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        String dni = request.getParameter("dni");
        Miembro miembro = miembroDAO.findByDni(dni);
        List<ComisionMiembro> comisiones = null;
        if (miembro != null) {
            comisiones = comisionMiembroDAO.findByMiembroId(miembro. getId());
        }
        request.setAttribute("miembro", miembro);
        request.setAttribute("comisiones", comisiones);
        request.setAttribute("dniBuscado", dni);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarPorDni.jsp");
        dispatcher.forward(request, response);
    }

    private void showBuscarComisionForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarComision.jsp");
        dispatcher.forward(request, response);
    }

    private void buscarComisionPorNombre(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        String nombre = request.getParameter("nombre");
        List<Comision> comisiones = comisionDAO.findByNombreLike(nombre);
        
        for (Comision comision : comisiones) {
            List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(comision.getId());
            comision.setMiembros(new HashSet<>(miembros));
        }
        request.setAttribute("comisiones", comisiones);
        request.setAttribute("nombreBuscado", nombre);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarComision.jsp");
        dispatcher.forward(request, response);
    }

    private void bajaMiembro(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ParseException {
        String[] parts = request.getPathInfo().split("/");
        Long comisionId = Long.parseLong(parts[2]);
        Long miembroId = Long.parseLong(parts[3]);
        String fechaBajaStr = request.getParameter("fechaBaja");
        
        System.out.println("[bajaMiembro] comisionId=" + comisionId + ", miembroId=" + miembroId + ", fechaBajaStr=" + fechaBajaStr);
        
        Date fechaBajaUtil = new SimpleDateFormat("yyyy-MM-dd").parse(fechaBajaStr);
        java.sql.Date fechaBaja = new java.sql.Date(fechaBajaUtil.getTime());
        comisionMiembroDAO.darDeBaja(comisionId, miembroId, fechaBaja);
        
        System.out.println("[bajaMiembro] Redirecting to:  " + request.getContextPath() + "/comisiones/view/" + comisionId);
        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }
   
    // Endpoint AJAX para obtener comisiones existentes
    private void getComisionesExistentes(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        String areaStr = request.getParameter("area");
        String tipoStr = request.getParameter("tipo");
        
        response.setContentType("application/json;charset=UTF-8");
        
        System.out.println("===> getComisionesExistentes:  area=" + areaStr + ", tipo=" + tipoStr);
        
        if (areaStr == null || tipoStr == null) {
            response.getWriter().write("{\"error\": \"Parámetros area y tipo requeridos\"}");
            return;
        }
        
        try {
            Comision. Area area = Comision.Area.valueOf(areaStr);
            Comision.Tipo tipo = Comision.Tipo. valueOf(tipoStr);
            
            List<Comision> comisiones = comisionDAO. findByAreaAndTipo(area, tipo);
            
            System.out.println("===> Comisiones encontradas: " + comisiones.size());
            
            // Construir JSON manualmente
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < comisiones.size(); i++) {
                Comision c = comisiones.get(i);
                json.append("{\"id\": ").append(c.getId())
                    .append(",\"nombre\": \"").append(c.getNombre().replace("\"", "\\\"")).append("\"}");
                if (i < comisiones.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            
            String jsonString = json.toString();
            System.out.println("===> JSON generado: " + jsonString);
            response.getWriter().write(jsonString);
            
        } catch (IllegalArgumentException e) {
            System.err.println("===> Error: " + e.getMessage());
            response.getWriter().write("{\"error\": \"Valores de area o tipo inválidos\"}");
        }
    }

    /**
     * ⭐ ACTUALIZADO: Procesar miembros desde JSON con validación de duplicados
     */
    private void procesarMiembrosJSON(Long comisionId, String miembrosJSON) throws SQLException, ParseException {
        System.out.println("========================================");
        System.out.println("===> Procesando miembros JSON: " + miembrosJSON);
        System.out.println("========================================");
        
        miembrosJSON = miembrosJSON.trim();
        if (miembrosJSON.startsWith("[")) {
            miembrosJSON = miembrosJSON.substring(1, miembrosJSON.length() - 1);
        }
        
        if (miembrosJSON.isEmpty()) {
            System.out. println("===> JSON vacío");
            return;
        }
        
        String[] miembrosArray = miembrosJSON.split("\\},\\{");
        
        int agregados = 0;
        int duplicados = 0;
        int errores = 0;
        
        for (String miembroStr : miembrosArray) {
            miembroStr = miembroStr.replace("{", "").replace("}", "").replace("\"", "");
            
            String dni = null, nombre = null, rol = "PARTICIPANTE", email = "";
            
            String[] campos = miembroStr.split(",");
            for (String campo :  campos) {
                String[] partes = campo.split(":", 2);
                if (partes.length == 2) {
                    String key = partes[0].trim();
                    String value = partes[1].trim();
                    
                    switch(key) {
                        case "dni":  dni = value; break;
                        case "nombre": nombre = value; break;
                        case "rol": rol = value; break;
                        case "email": email = value; break;
                    }
                }
            }
            
            if (dni == null || nombre == null) {
                System.out.println("⚠️  ADVERTENCIA: Miembro con datos incompletos.  Se omite.");
                errores++;
                continue;
            }
            
            System.out.println("\n--- Procesando miembro: " + nombre + " (" + dni + ") - Rol: " + rol + " ---");
            
            try {
                // Verificar si el miembro ya existe en la base de datos
                Miembro miembro = miembroDAO.findByDni(dni);
                if (miembro == null) {
                    // Crear nuevo miembro
                    miembro = new Miembro();
                    miembro.setDniNif(dni);
                    miembro.setNombreApellidos(nombre);
                    miembro.setEmail(email);
                    miembroDAO.save(miembro);
                    System.out.println("✅ Nuevo miembro creado con ID: " + miembro.getId());
                } else {
                    System.out.println("ℹ️  Miembro existente encontrado con ID: " + miembro.getId());
                }
                
                // ⭐ VALIDACIÓN: Verificar si ya está en esta comisión
                if (comisionMiembroDAO.existeEnComision(comisionId, miembro.getId())) {
                    System.out.println("⚠️  DUPLICADO: El miembro " + nombre + " (" + dni + ") ya pertenece a esta comisión.  Se omite.");
                    duplicados++;
                    continue;
                }
                
                // Agregar a la comisión
                Comision comision = comisionDAO.findById(comisionId);
                ComisionMiembro cm = new ComisionMiembro();
                cm.setComision(comision);
                cm.setMiembro(miembro);
                cm. setCargo(ComisionMiembro.Cargo.valueOf(rol));
                cm.setFechaIncorporacion(new java.sql.Date(System.currentTimeMillis()));
                
                comisionMiembroDAO.save(cm);
                System.out. println("✅ Miembro agregado exitosamente a la comisión");
                agregados++;
                
                // Mostrar en cuántas comisiones está ahora
                int totalComisiones = comisionMiembroDAO.contarComisionesActivas(miembro.getId());
                System.out.println("ℹ️  El miembro ahora pertenece a " + totalComisiones + " comisión(es) activa(s)");
                
            } catch (Exception e) {
                System.err.println("❌ ERROR procesando miembro " + nombre + " (" + dni + "): " + e.getMessage());
                e.printStackTrace();
                errores++;
            }
        }
        
        System.out.println("\n========================================");
        System.out. println("✅ RESUMEN DE PROCESAMIENTO:");
        System.out.println("   Total en JSON: " + miembrosArray.length);
        System.out.println("   ✅ Agregados exitosamente: " + agregados);
        System.out.println("   ⚠️  Duplicados omitidos: " + duplicados);
        System.out.println("   ❌ Errores: " + errores);
        System.out.println("========================================\n");
    }
}
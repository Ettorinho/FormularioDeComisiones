package com.comisiones.controller;

import com.comisiones.dao.ActaDAO;
import com.comisiones.dao.ComisionDAO;
import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.HistorialCargoDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.Acta;
import com.comisiones.dto.ComisionDTO;
import com.comisiones.dto.MiembroDTO;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.HistorialCargo;
import com.comisiones.model.Miembro;
import com.comisiones.service.AuditoriaService;
import com.comisiones.util.AppLogger;
import com.comisiones.util.ServletHelper;
import com.comisiones.util.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;

@WebServlet("/comisiones/*")
public class ComisionController extends HttpServlet {

    private ComisionDAO comisionDAO;
    private ComisionMiembroDAO comisionMiembroDAO;
    private MiembroDAO miembroDAO;
    private ActaDAO actaDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        comisionDAO = new ComisionDAO();
        comisionMiembroDAO = new ComisionMiembroDAO();
        miembroDAO = new MiembroDAO();
        actaDAO = new ActaDAO();
        objectMapper = new ObjectMapper();
        AppLogger.info("ComisionController INICIALIZADO");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            showIndexView(request, response);
            return;
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
                        listComisiones(request, response);
                        break;
                    default:
                        showIndexView(request, response);
                        break;
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Error en la operación GET", e);
        }
    }

    private void showIndexView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/index.jsp");
        dispatcher.forward(request, response);
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
        cargarCatalogos(request);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/form.jsp");
        dispatcher.forward(request, response);
    }

    private void viewComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Long id = ServletHelper.parsePathId(request, "/view/");
        if (id == null) {
            ServletHelper.forwardError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID de comisión no válido en la URL");
            return;
        }

        Comision comision = comisionDAO.findById(id);
        List<ComisionMiembro> miembros = comisionMiembroDAO.findByComisionId(id);
        List<Acta> actas = actaDAO.findByComisionId(id);
        request.setAttribute("comision", comision);
        request.setAttribute("miembros", miembros);
        request.setAttribute("actas", actas);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/view.jsp");
        dispatcher.forward(request, response);
    }

    private void showAddMemberForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Long comisionId = ServletHelper.parsePathId(request, "/addMember/");
        if (comisionId == null) {
            ServletHelper.forwardError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID de comisión no válido");
            return;
        }
        Comision comision = comisionDAO.findById(comisionId);
        request.setAttribute("comision", comision);
        request.setAttribute("cargos", ComisionMiembro.Cargo.values());
        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", error);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/addMember.jsp");
        dispatcher.forward(request, response);
    }

    private void showBajaMiembrosForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        Long comisionId = ServletHelper.parsePathId(request, "/bajaMiembros/");
        if (comisionId == null) {
            ServletHelper.forwardError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID de comisión no válido");
            return;
        }
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
        AppLogger.debug("saveComision INVOCADO");
        
        String nombre = request.getParameter("nombre");
        String areaStr = request.getParameter("area");
        String tipoStr = request.getParameter("tipo");
        String fechaConstitucionStr = request.getParameter("fechaConstitucion");
        String fechaFinStr = request.getParameter("fechaFin");
        String miembrosJSON = request.getParameter("miembrosJSON");
        String opcionCreacion = request.getParameter("opcionCreacion");
        String comisionExistenteStr = request.getParameter("comisionExistente");
        
        AppLogger.debug("Parámetros recibidos - nombre: " + nombre + ", area: " + areaStr + 
                       ", tipo: " + tipoStr + ", opcionCreacion: " + opcionCreacion);
        
        // Validar área y tipo
        if (areaStr == null || tipoStr == null) {
            request.setAttribute("error", "Debe seleccionar área y tipo");
            showNewForm(request, response);
            return;
        }
        
        Long comisionId = null;
        
        // Opción 1: Agregar miembros a comisión existente
        if ("existente".equals(opcionCreacion) && comisionExistenteStr != null && !comisionExistenteStr.isEmpty()) {
            comisionId = ServletHelper.parseIdSafely(comisionExistenteStr);
            if (comisionId == null) {
                request.setAttribute("error", "ID de comisión existente no válido: " + comisionExistenteStr);
                showNewForm(request, response);
                return;
            }
            AppLogger.debug("Agregando miembros a comisión existente ID: " + comisionId);
        } 
        // Opción 2: Crear nueva comisión
        else {
            if (nombre == null || nombre.trim().isEmpty() || fechaConstitucionStr == null) {
                request.setAttribute("error", "Debe proporcionar nombre y fecha de constitución");
                showNewForm(request, response);
                return;
            }
            
            Comision nuevaComision = new Comision();
            nuevaComision.setNombre(nombre.trim());
            nuevaComision.setArea(Comision.Area.valueOf(areaStr));
            nuevaComision.setTipo(Comision.Tipo.valueOf(tipoStr));

            Date fechaConst = new SimpleDateFormat("yyyy-MM-dd").parse(fechaConstitucionStr);
            nuevaComision.setFechaConstitucion(new java.sql.Date(fechaConst.getTime()));

            if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
                Date fechaFin = new SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
                nuevaComision.setFechaFin(new java.sql.Date(fechaFin.getTime()));
            }

            Map<String, String> erroresValidacionComision = ValidationUtil.validateWithFields(nuevaComision);
            if (!erroresValidacionComision.isEmpty()) {
                request.setAttribute("error", "Datos de comisión inválidos: " + formatValidationErrors(erroresValidacionComision));
                showNewForm(request, response);
                return;
            }
            
            // Verificar si ya existe
            if (comisionDAO.exists(nombre.trim(), nuevaComision.getArea(), nuevaComision.getTipo())) {
                request.setAttribute("error", "Ya existe una " + nuevaComision.getTipo().getDescripcion() + 
                                              " con ese nombre en " + nuevaComision.getArea().getDescripcion());
                showNewForm(request, response);
                return;
            }
            
            comisionDAO.save(nuevaComision);
            comisionId = nuevaComision.getId();
            AppLogger.info("Nueva comisión creada con ID: " + comisionId);
            AuditoriaService.getInstance().registrar(request, ServletHelper.getUsuarioLogueado(request),
                "CREAR", "COMISION", nuevaComision.getId().toString(),
                "Creó la comisión: " + nuevaComision.getNombre());
        }
        
        // Procesar miembros desde JSON
        if (miembrosJSON != null && !miembrosJSON.isEmpty() && !miembrosJSON.equals("[]")) {
            AppLogger.debug("Procesando miembros JSON");
            procesarMiembrosJSON(comisionId, miembrosJSON);
            if ("existente".equals(opcionCreacion)) {
                AuditoriaService.getInstance().registrar(request, ServletHelper.getUsuarioLogueado(request),
                    "MODIFICAR", "COMISION", comisionId.toString(),
                    "Agregó miembros a la comisión ID: " + comisionId);
            }
        } else {
            AppLogger.debug("No hay miembros para procesar");
        }

        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }

    private void saveMemberInComision(HttpServletRequest request, HttpServletResponse response) throws SQLException, ParseException, IOException {
        Long comisionId = ServletHelper.parsePathId(request, "/addMember/");
        if (comisionId == null) {
            response.sendRedirect(request.getContextPath() + "/comisiones?error=ID+invalido");
            return;
        }
        String nombreApellidos = request.getParameter("nombreApellidos");
        String dni              = request.getParameter("dni");
        String email            = request.getParameter("email");
        String cargoStr         = request.getParameter("cargo");
        String fechaIncorporacionStr = request.getParameter("fechaIncorporacion");

        // 1. ¿El miembro ya existe? Si es así, reutilizarlo.
        Miembro miembro = miembroDAO.findByDni(dni);
        if (miembro == null) {
            miembro = new Miembro();
            miembro.setNombreApellidos(nombreApellidos);
            miembro.setDniNif(dni);
            miembro.setEmail(email);

            Map<String, String> erroresValidacionMiembro = ValidationUtil.validateWithFields(miembro);
            if (!erroresValidacionMiembro.isEmpty()) {
                response.sendRedirect(request.getContextPath()
                        + "/comisiones/addMember/" + comisionId
                        + "?error=" + java.net.URLEncoder.encode("Datos de miembro inválidos: "
                        + formatValidationErrors(erroresValidacionMiembro), java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
            miembroDAO.save(miembro);
        }

        // 2. ¿Ya está asignado (activo) a esta comisión?
        if (comisionMiembroDAO.existeEnComision(comisionId, miembro.getId())) {
            response.sendRedirect(request.getContextPath()
                    + "/comisiones/addMember/" + comisionId
                    + "?error=El+miembro+ya+pertenece+a+esta+comisi%C3%B3n");
            return;
        }

        // 3. Crear la relación comisión–miembro
        Comision comision = comisionDAO.findById(comisionId);
        Date fechaIncorp = new SimpleDateFormat("yyyy-MM-dd").parse(fechaIncorporacionStr);

        ComisionMiembro comisionMiembro = new ComisionMiembro();
        comisionMiembro.setComision(comision);
        comisionMiembro.setMiembro(miembro);
        comisionMiembro.setCargo(ComisionMiembro.Cargo.valueOf(cargoStr.toUpperCase()));
        comisionMiembro.setFechaIncorporacion(new java.sql.Date(fechaIncorp.getTime()));

        Map<String, String> erroresValidacionRelacion = ValidationUtil.validateWithFields(comisionMiembro);
        if (!erroresValidacionRelacion.isEmpty()) {
            response.sendRedirect(request.getContextPath()
                    + "/comisiones/addMember/" + comisionId
                    + "?error=" + java.net.URLEncoder.encode("Datos de incorporación inválidos: "
                    + formatValidationErrors(erroresValidacionRelacion), java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        comisionMiembroDAO.save(comisionMiembro);
        AuditoriaService.getInstance().registrar(request, ServletHelper.getUsuarioLogueado(request),
            "CREAR", "MIEMBRO", comisionId + "/" + miembro.getId(),
            "Agregó al miembro " + miembro.getNombreApellidos() + " (DNI: " + miembro.getDniNif()
                + ") a la comisión ID: " + comisionId + " con cargo: " + cargoStr);
        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }

    private void showBuscarPorDniForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarPorDni.jsp");
        dispatcher.forward(request, response);
    }

    private void buscarComisionesPorDni(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        String dni = request.getParameter("dni");
        Miembro miembro = miembroDAO.findByDni(dni);
        List<ComisionMiembro> comisiones = null;
        Map<String, List<HistorialCargo>> historialPorComision = new java.util.HashMap<>();

        if (miembro != null) {
            comisiones = comisionMiembroDAO.findByMiembroId(miembro.getId());
            if (comisiones != null && !comisiones.isEmpty()) {
                HistorialCargoDAO historialDAO = new HistorialCargoDAO();
                Map<Long, List<HistorialCargo>> historialAgrupado = historialDAO.getHistorialAgrupadoPorMiembro(miembro.getId());
                for (ComisionMiembro cm : comisiones) {
                    historialPorComision.put(
                            cm.getComision().getId().toString(),
                            historialAgrupado.getOrDefault(cm.getComision().getId(), new ArrayList<>()));
                }
            }
        }
        request.setAttribute("miembro", miembro);
        request.setAttribute("comisiones", comisiones);
        request.setAttribute("dniBuscado", dni);
        request.setAttribute("historialPorComision", historialPorComision);
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

        Map<Long, List<ComisionMiembro>> miembrosPorComision = comisionMiembroDAO.findByComisionIds(
                comisiones.stream().map(Comision::getId).collect(Collectors.toList()));
        for (Comision comision : comisiones) {
            List<ComisionMiembro> miembros = miembrosPorComision.getOrDefault(comision.getId(), new ArrayList<>());
            comision.setMiembros(new HashSet<>(miembros));
        }
        request.setAttribute("comisiones", comisiones);
        request.setAttribute("nombreBuscado", nombre);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/comisiones/buscarComision.jsp");
        dispatcher.forward(request, response);
    }

    private void bajaMiembro(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ParseException {
        Long[] ids = ServletHelper.parsePathIds(request, "/bajaMiembro/", 2);
        Long comisionId = ids != null ? ids[0] : null;
        Long miembroId = ids != null ? ids[1] : null;
        
        if (comisionId == null || miembroId == null) {
            response.sendRedirect(request.getContextPath() + "/comisiones?error=IDs+invalidos");
            return;
        }
        
        String fechaBajaStr = request.getParameter("fechaBaja");
        
        AppLogger.debug("bajaMiembro - comisionId: " + comisionId + ", miembroId: " + miembroId);
        
        Date fechaBajaUtil = new SimpleDateFormat("yyyy-MM-dd").parse(fechaBajaStr);
        java.sql.Date fechaBaja = new java.sql.Date(fechaBajaUtil.getTime());
        comisionMiembroDAO.darDeBaja(comisionId, miembroId, fechaBaja);
        AuditoriaService.getInstance().registrar(request, ServletHelper.getUsuarioLogueado(request),
            "BAJA", "MIEMBRO", comisionId + "/" + miembroId,
            "Dio de baja al miembro ID: " + miembroId + " de la comisión ID: " + comisionId
                + " con fecha: " + fechaBajaStr);
        
        AppLogger.debug("Redirecting to /comisiones/view/" + comisionId);
        response.sendRedirect(request.getContextPath() + "/comisiones/view/" + comisionId);
    }
   
    // Endpoint AJAX para obtener comisiones existentes
    private void getComisionesExistentes(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        String areaStr = request.getParameter("area");
        String tipoStr = request.getParameter("tipo");
        
        response.setContentType("application/json;charset=UTF-8");
        
        AppLogger.debug("getComisionesExistentes - area: " + areaStr + ", tipo: " + tipoStr);
        
        if (areaStr == null || tipoStr == null) {
            ServletHelper.sendBadRequest(response, "Parámetros area y tipo requeridos");
            return;
        }
        
        try {
            Comision.Area area = Comision.Area.valueOf(areaStr);
            Comision.Tipo tipo = Comision.Tipo.valueOf(tipoStr);
            
            List<Comision> comisiones = comisionDAO.findByAreaAndTipo(area, tipo);
            
            AppLogger.debug("Comisiones encontradas: " + comisiones.size());
            
            // Convertir a DTOs y serializar con Jackson
            List<ComisionDTO> dtos = new ArrayList<>();
            for (Comision c : comisiones) {
                dtos.add(new ComisionDTO(c.getId(), c.getNombre()));
            }
            
            String json = objectMapper.writeValueAsString(dtos);
            AppLogger.debug("JSON generado con " + dtos.size() + " comisiones");
            response.getWriter().write(json);
            
        } catch (IllegalArgumentException e) {
            AppLogger.error("Error: valores de area o tipo inválidos", e);
            ServletHelper.sendBadRequest(response, "Valores de area o tipo inválidos");
        }
    }

    /**
     * ⭐ ACTUALIZADO: Procesar miembros desde JSON con validación de duplicados
     * Ahora usa Jackson para parsing seguro
     */
    private void procesarMiembrosJSON(Long comisionId, String miembrosJSON) throws SQLException {
        AppLogger.debug("Procesando miembros JSON: " + miembrosJSON);
        
        try {
            // Deserializar con Jackson
            MiembroDTO[] miembros = objectMapper.readValue(miembrosJSON, MiembroDTO[].class);
            Comision comision = comisionDAO.findById(comisionId);
            
            int agregados = 0;
            int duplicados = 0;
            int errores = 0;
            
            for (MiembroDTO dto : miembros) {
                // Validar datos del miembro
                if (!dto.isValid()) {
                    AppLogger.debug("ADVERTENCIA: Miembro con datos incompletos. Se omite: " + dto);
                    errores++;
                    continue;
                }
                
                String dni = dto.getDni().trim();
                String nombre = dto.getNombre().trim();
                String rol = dto.getRol().toUpperCase();
                String email = dto.getEmail().trim();
                
                AppLogger.debug("Procesando miembro: " + nombre + " (" + dni + ") - Rol: " + rol);
                
                try {
                    // Verificar si el miembro ya existe en la base de datos
                    Miembro miembro = miembroDAO.findByDni(dni);
                    if (miembro == null) {
                        // Crear nuevo miembro
                        miembro = new Miembro();
                        miembro.setDniNif(dni);
                        miembro.setNombreApellidos(nombre);
                        miembro.setEmail(email);

                        Map<String, String> erroresMiembro = ValidationUtil.validateWithFields(miembro);
                        if (!erroresMiembro.isEmpty()) {
                            AppLogger.warn("Miembro inválido omitido (" + dni + "): " + formatValidationErrors(erroresMiembro));
                            errores++;
                            continue;
                        }
                        miembroDAO.save(miembro);
                        AppLogger.debug("Nuevo miembro creado con ID: " + miembro.getId());
                    } else {
                        AppLogger.debug("Miembro existente encontrado con ID: " + miembro.getId());
                    }
                    
                    // ⭐ VALIDACIÓN: Verificar si ya está en esta comisión
                    if (comisionMiembroDAO.existeEnComision(comisionId, miembro.getId())) {
                        AppLogger.debug("DUPLICADO: El miembro " + nombre + " ya pertenece a esta comisión. Se omite.");
                        duplicados++;
                        continue;
                    }
                    // Agregar a la comisión
                    // Agregar a la comisión
                    ComisionMiembro cm = new ComisionMiembro();
                    cm.setComision(comision);
                    cm.setMiembro(miembro);
                    cm.setCargo(ComisionMiembro.Cargo.valueOf(rol));
                    cm.setFechaIncorporacion(new java.sql.Date(System.currentTimeMillis()));

                    Map<String, String> erroresRelacion = ValidationUtil.validateWithFields(cm);
                    if (!erroresRelacion.isEmpty()) {
                        AppLogger.warn("Relación comisión-miembro inválida omitida (" + dni + "): "
                                + formatValidationErrors(erroresRelacion));
                        errores++;
                        continue;
                    }
                    
                    comisionMiembroDAO.save(cm);
                    AppLogger.debug("Miembro agregado exitosamente a la comisión");
                    agregados++;
                    
                    // Mostrar en cuántas comisiones está ahora
                    int totalComisiones = comisionMiembroDAO.contarComisionesActivas(miembro.getId());
                    AppLogger.debug("El miembro ahora pertenece a " + totalComisiones + " comisión(es) activa(s)");
                    
                } catch (Exception e) {
                    AppLogger.error("ERROR procesando miembro " + nombre + " (" + dni + ")", e);
                    errores++;
                }
            }
            
            AppLogger.info("RESUMEN DE PROCESAMIENTO: Total=" + miembros.length + 
                          ", Agregados=" + agregados + ", Duplicados=" + duplicados + ", Errores=" + errores);
                          
        } catch (Exception e) {
            AppLogger.error("ERROR al parsear JSON de miembros", e);
            throw new SQLException("Error al procesar miembros desde JSON: " + e.getMessage(), e);
        }
    }

    private void cargarCatalogos(HttpServletRequest request) {
        request.setAttribute("areas", Comision.Area.values());
        request.setAttribute("tipos", Comision.Tipo.values());
        request.setAttribute("cargos", ComisionMiembro.Cargo.values());
    }

    private String formatValidationErrors(Map<String, String> fieldErrors) {
        Map<String, String> orderedErrors = new LinkedHashMap<>(fieldErrors);
        return orderedErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
    }
}
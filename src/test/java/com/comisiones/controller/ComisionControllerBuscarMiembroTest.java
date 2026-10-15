package com.comisiones.controller;

import com.comisiones.dao.ComisionMiembroDAO;
import com.comisiones.dao.MiembroDAO;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComisionControllerBuscarMiembroTest {

    private ComisionController controller;
    private StubMiembroDAO miembroDAO;
    private final Map<String, Object> attributes = new HashMap<>();
    private Long miembroCargado;
    private boolean forwarded;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ComisionController();
        miembroDAO = new StubMiembroDAO();
        setField("miembroDAO", miembroDAO);
        setField("comisionMiembroDAO", new ComisionMiembroDAO() {
            @Override
            public List<ComisionMiembro> findByMiembroId(Long id) {
                miembroCargado = id;
                return Collections.emptyList();
            }
        });
    }

    @Test
    void combinedDniSearchUsesExactLookupAndLoadsDetail() throws Exception {
        Miembro miembro = miembro(1L, "Ana García", "12345678Z");
        miembroDAO.exacto = miembro;

        search("busqueda", " 12345678Z ");

        assertEquals("12345678Z", miembroDAO.dniBuscado);
        assertNull(miembroDAO.nombreBuscado);
        assertDetail(miembro);
    }

    @Test
    void combinedNieSearchUsesExactLookup() throws Exception {
        search("busqueda", "X1234567L");

        assertEquals("X1234567L", miembroDAO.dniBuscado);
        assertNull(miembroDAO.nombreBuscado);
    }

    @Test
    void legacyDniAndCandidateSelectionRemainExactEvenForNonstandardDni() throws Exception {
        Miembro miembro = miembro(2L, "Ana López", "DNI-antiguo");
        miembroDAO.exacto = miembro;

        search("dni", miembro.getDniNif());

        assertEquals(miembro.getDniNif(), miembroDAO.dniBuscado);
        assertNull(miembroDAO.nombreBuscado);
        assertDetail(miembro);
    }

    @Test
    void singleNameMatchLoadsTheSameDetailAttributes() throws Exception {
        Miembro miembro = miembro(3L, "Ana García López", "87654321X");
        miembroDAO.coincidencias = Collections.singletonList(miembro);

        search("busqueda", "  gArCía  ");

        assertEquals("gArCía", miembroDAO.nombreBuscado);
        assertEquals("gArCía", attributes.get("busquedaBuscada"));
        assertNull(miembroDAO.dniBuscado);
        assertDetail(miembro);
    }

    @Test
    void multipleNameMatchesShowCandidatesWithoutLoadingCommissions() throws Exception {
        miembroDAO.coincidencias = List.of(
                miembro(1L, "Ana García", "12345678Z"),
                miembro(2L, "Ana López", "87654321X"));

        search("busqueda", "Ana");

        assertEquals(miembroDAO.coincidencias, attributes.get("miembrosEncontrados"));
        assertNull(attributes.get("miembro"));
        assertNull(miembroCargado);
    }

    @Test
    void noNameMatchesLeaveDetailAndCandidatesEmpty() throws Exception {
        search("busqueda", "Desconocido");

        assertEquals("Desconocido", miembroDAO.nombreBuscado);
        assertNull(attributes.get("miembro"));
        assertNull(attributes.get("miembrosEncontrados"));
        assertNull(miembroCargado);
    }

    @Test
    void noDniMatchDoesNotFallBackToNameSearch() throws Exception {
        search("busqueda", "12345678Z");

        assertNull(miembroDAO.nombreBuscado);
        assertNull(attributes.get("miembro"));
        assertNull(miembroCargado);
    }

    @Test
    void emptyOrMissingSearchDoesNotQueryTheDatabase() throws Exception {
        search("busqueda", "   ");
        search("busqueda", null);

        assertNull(miembroDAO.dniBuscado);
        assertNull(miembroDAO.nombreBuscado);
        assertNull(miembroCargado);
    }

    private void assertDetail(Miembro miembro) {
        assertSame(miembro, attributes.get("miembro"));
        assertEquals(miembro.getId(), miembroCargado);
        assertEquals(Collections.emptyList(), attributes.get("comisiones"));
        assertEquals(Collections.emptyMap(), attributes.get("historialPorComision"));
        assertEquals(Collections.emptyMap(), attributes.get("porcentajeExcusaPorComision"));
        assertEquals(50.0, attributes.get("umbralExcusaAsistenciaAlerta"));
        assertNull(attributes.get("miembrosEncontrados"));
    }

    private void search(String parameter, String value) throws Exception {
        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(), new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        forwarded = true;
                    }
                    return null;
                });
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getPathInfo":
                            return "/buscarPorDni";
                        case "getParameter":
                            return parameter.equals(args[0]) ? value : null;
                        case "setAttribute":
                            attributes.put((String) args[0], args[1]);
                            return null;
                        case "getRequestDispatcher":
                            assertEquals("/WEB-INF/views/comisiones/buscarPorDni.jsp", args[0]);
                            return dispatcher;
                        default:
                            throw new AssertionError("Unexpected request call: " + method.getName());
                    }
                });
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    throw new AssertionError("Unexpected response call: " + method.getName());
                });

        controller.doPost(request, response);
        assertTrue(forwarded);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = ComisionController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private Miembro miembro(Long id, String nombre, String dni) {
        Miembro miembro = new Miembro();
        miembro.setId(id);
        miembro.setNombreApellidos(nombre);
        miembro.setDniNif(dni);
        return miembro;
    }

    private static class StubMiembroDAO extends MiembroDAO {
        private Miembro exacto;
        private List<Miembro> coincidencias = Collections.emptyList();
        private String dniBuscado;
        private String nombreBuscado;

        @Override
        public Miembro findByDni(String dni) {
            dniBuscado = dni;
            return exacto;
        }

        @Override
        public List<Miembro> findByNombreLike(String texto) {
            nombreBuscado = texto;
            return coincidencias;
        }
    }
}

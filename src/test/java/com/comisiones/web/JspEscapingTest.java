package com.comisiones.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class JspEscapingTest {

    @Test
    void memberSearchEscapesCandidatesAndPreservesCsrfAndFilters() throws IOException {
        String view = Files.readString(resolve("/src/main/webapp/WEB-INF/views/comisiones/buscarPorDni.jsp"));
        assertTrue(view.contains("name=\"busqueda\""));
        assertTrue(view.contains("<c:out value='${busquedaBuscada"));
        assertTrue(view.contains("<c:out value=\"${candidato.nombreApellidos}\"/>"));
        assertTrue(view.contains("<c:out value=\"${candidato.dniNif}\"/>"));
        assertTrue(view.contains("name=\"dni\" value=\"<c:out value='${candidato.dniNif}'/>\""));
        assertTrue(view.contains("No se encontró ningún miembro con ese DNI/NIF o nombre y apellidos."));
        assertTrue(view.contains("id=\"mostrarActivas\""));
        assertTrue(view.contains("id=\"mostrarFinalizadas\""));
        for (String form : view.split("<form ")) {
            if (form.contains("method=\"post\"")) {
                assertTrue(form.substring(0, form.indexOf("</form>")).contains("name=\"csrfToken\""));
            }
        }
    }

    @Test
    void jspViewsEscapePreviouslyRawUserControlledExpressions() throws IOException {
        assertContains("/src/main/webapp/WEB-INF/views/common/header.jspf", "<c:out value=\"${sessionScope.usuarioLogueado.nombreCompleto}\"/>");
        assertContains("/src/main/webapp/index.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/miembros/list.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/list.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/actas/form.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/actas/view.jsp", "<c:out value=\"${acta.observaciones}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/actas/view.jsp", "<c:out value=\"${asistencia.justificacion}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/actas/miembros-fragment.jsp", "<c:out value=\"${miembro.nombreApellidos}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/error.jsp", "<c:out value=\"${error != null ? error : 'No se proporcionó un mensaje de error específico.'}\"/>");

        assertNotContains("/src/main/webapp/WEB-INF/views/actas/view.jsp", ">${acta.observaciones}<");
        assertNotContains("/src/main/webapp/WEB-INF/views/comisiones/buscarPorDni.jsp", "👤 ${miembro.nombreApellidos}");
        assertNotContains("/src/main/webapp/WEB-INF/views/comisiones/buscarComision.jsp", "<strong>${comision.nombre}</strong>");
        assertNotContains("/src/main/webapp/WEB-INF/views/error.jsp", "${error != null ? error : \"No se proporcionó un mensaje de error específico.\"}");
    }

    private void assertContains(String relativePath, String expectedSnippet) throws IOException {
        String content = Files.readString(resolve(relativePath));
        assertTrue(content.contains(expectedSnippet),
                () -> relativePath + " should contain escaped snippet: " + expectedSnippet);
    }

    private void assertNotContains(String relativePath, String forbiddenSnippet) throws IOException {
        String content = Files.readString(resolve(relativePath));
        assertFalse(content.contains(forbiddenSnippet),
                () -> relativePath + " should not contain raw snippet: " + forbiddenSnippet);
    }

    private Path resolve(String relativePath) {
        return Path.of("." + relativePath);
    }
}

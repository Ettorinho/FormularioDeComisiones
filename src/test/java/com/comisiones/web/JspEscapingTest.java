package com.comisiones.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class JspEscapingTest {

    @Test
    void jspViewsEscapePreviouslyRawUserControlledExpressions() throws IOException {
        assertContains("/src/main/webapp/index.jsp", "<c:out value=\"${sessionScope.usuarioLogueado.nombreCompleto}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/miembros/list.jsp", "<c:out value=\"${sessionScope.usuarioLogueado.nombreCompleto}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/list.jsp", "<c:out value=\"${sessionScope.usuarioLogueado.nombreCompleto}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "<c:out value=\"${sessionScope.usuarioLogueado.nombreCompleto}\"/>");
        assertContains("/src/main/webapp/WEB-INF/views/actas/form.jsp", "<c:out value=\"${sessionScope.usuarioLogueado.nombreCompleto}\"/>");
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

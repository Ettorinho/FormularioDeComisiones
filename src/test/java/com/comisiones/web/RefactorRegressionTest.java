package com.comisiones.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RefactorRegressionTest {

    @Test
    void controllersUseSharedServletHelperForSessionAndPathParsing() throws IOException {
        assertContains("/src/main/java/com/comisiones/controller/ActaController.java",
                "ServletHelper.getUsuarioLogueado(request)");
        assertContains("/src/main/java/com/comisiones/controller/CambiarCargoServlet.java",
                "ServletHelper.getUsuarioLogueado(request)");
        assertContains("/src/main/java/com/comisiones/controller/ComisionController.java",
                "ServletHelper.getUsuarioLogueado(request)");
        assertContains("/src/main/java/com/comisiones/controller/ComisionController.java",
                "ServletHelper.parsePathId(request, \"/view/\")");
        assertContains("/src/main/java/com/comisiones/controller/ComisionController.java",
                "ServletHelper.parsePathIds(request, \"/bajaMiembro/\", 2)");
        assertNotContains("/src/main/java/com/comisiones/controller/ActaController.java",
                "private String getUsuarioLogueado");
        assertNotContains("/src/main/java/com/comisiones/controller/CambiarCargoServlet.java",
                "private String getUsuarioLogueado");
        assertNotContains("/src/main/java/com/comisiones/controller/ComisionController.java",
                "private String getUsuarioLogueado");
    }

    @Test
    void controllerAndDaoBatchQueriesAvoidPreviousNPlusOnePatterns() throws IOException {
        assertContains("/src/main/java/com/comisiones/dao/ComisionMiembroDAO.java", "findByComisionIds(");
        assertContains("/src/main/java/com/comisiones/dao/HistorialCargoDAO.java", "getHistorialAgrupadoPorMiembro(");
        assertContains("/src/main/java/com/comisiones/controller/ComisionController.java", "findByComisionIds(");
        assertContains("/src/main/java/com/comisiones/controller/ComisionController.java", "getHistorialAgrupadoPorMiembro(");
    }

    @Test
    void jspViewsUseSharedIncludesDynamicCatalogsAndExternalScripts() throws IOException {
        assertContains("/src/main/webapp/index.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/index.jsp", "/WEB-INF/views/common/footer.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/index.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/index.jsp", "/WEB-INF/views/common/footer.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/addMember.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/addMember.jsp", "/WEB-INF/views/common/footer.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/actas/form.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/actas/form.jsp", "/WEB-INF/views/common/footer.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/actas/view.jsp", "/WEB-INF/views/common/header.jspf");
        assertContains("/src/main/webapp/WEB-INF/views/actas/view.jsp", "/WEB-INF/views/common/footer.jspf");

        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "items=\"${areas}\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "items=\"${tipos}\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "items=\"${cargos}\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/addMember.jsp", "items=\"${cargos}\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/cambiarCargo.jsp", "items=\"${cargos}\"");

        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "/resources/js/comisiones.js");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/addMember.jsp", "/resources/js/comisiones.js");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/buscarPorDni.jsp", "/resources/js/comisiones.js");
        assertContains("/src/main/webapp/WEB-INF/views/actas/form.jsp", "/resources/js/actas.js");
    }

    @Test
    void loggingIsBackedBySlf4jAndNoDirectConsolePrintingRemainsInMainCode() throws IOException {
        assertContains("/pom.xml", "<artifactId>slf4j-api</artifactId>");
        assertContains("/pom.xml", "<artifactId>logback-classic</artifactId>");
        assertContains("/pom.xml", "<artifactId>log4j-to-slf4j</artifactId>");

        try (Stream<Path> files = Files.walk(resolve("/src/main/java"))) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String content = Files.readString(path);
                    assertFalse(content.contains("System.out.println"),
                            () -> path + " should not use System.out.println directly");
                    assertFalse(content.contains("System.err.println"),
                            () -> path + " should not use System.err.println directly");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void assertContains(String relativePath, String expectedSnippet) throws IOException {
        String content = Files.readString(resolve(relativePath));
        assertTrue(content.contains(expectedSnippet),
                () -> relativePath + " should contain snippet: " + expectedSnippet);
    }

    private void assertNotContains(String relativePath, String forbiddenSnippet) throws IOException {
        String content = Files.readString(resolve(relativePath));
        assertFalse(content.contains(forbiddenSnippet),
                () -> relativePath + " should not contain snippet: " + forbiddenSnippet);
    }

    private Path resolve(String relativePath) {
        return Path.of("." + relativePath);
    }
}

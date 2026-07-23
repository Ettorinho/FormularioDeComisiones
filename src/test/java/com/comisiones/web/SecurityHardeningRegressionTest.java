package com.comisiones.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHardeningRegressionTest {

    @Test
    void csrfFilterIsRegisteredAndRejectsInvalidPostRequests() throws IOException {
        assertContains("/src/main/webapp/WEB-INF/web.xml", "<filter-name>CsrfFilter</filter-name>");
        assertContains("/src/main/java/com/comisiones/filter/CsrfFilter.java",
                "HttpServletResponse.SC_FORBIDDEN");
        assertContains("/src/main/java/com/comisiones/filter/CsrfFilter.java",
                "CsrfTokenUtil.isRequestTokenValid");
    }

    @Test
    void postFormsIncludeCsrfTokenField() throws IOException {
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/addMember.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/actas/form.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/cambiarCargo.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/form.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/bajaMiembros.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/buscarComision.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/comisiones/buscarPorDni.jsp", "name=\"csrfToken\"");
        assertContains("/src/main/webapp/WEB-INF/views/auth/login.jsp", "name=\"csrfToken\"");
    }

    @Test
    void runtimeControllersInvokeBeanValidationBeforePersisting() throws IOException {
        assertContains("/src/main/java/com/comisiones/controller/ComisionController.java",
                "ValidationUtil.validateWithFields");
        assertContains("/src/main/java/com/comisiones/controller/ActaController.java",
                "ValidationUtil.validateWithFields");
        assertContains("/src/main/java/com/comisiones/controller/CambiarCargoServlet.java",
                "ValidationUtil.validateWithFields");
    }

    @Test
    void ldapLookupDoesNotExposeInternalExceptionMessagesToClient() throws IOException {
        assertContains("/src/main/java/com/comisiones/ldap/LdapLookupServlet.java",
                "No se pudo completar la búsqueda LDAP. Contacte con el administrador si el problema persiste.");
        assertNotContains("/src/main/java/com/comisiones/ldap/LdapLookupServlet.java",
                "out.write(\"{\\\"error\\\":\\\"Error en consulta LDAP:");
    }

    @Test
    void ldapEnumerationsAreClosedExplicitly() throws IOException {
        assertContains("/src/main/java/com/comisiones/ldap/LdapLookupServlet.java", "results.close()");
        assertContains("/src/main/java/com/comisiones/ldap/LdapAuthService.java", "results.close()");
        assertContains("/src/main/java/com/comisiones/ldap/LdapAuthService.java", "valores.close()");
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

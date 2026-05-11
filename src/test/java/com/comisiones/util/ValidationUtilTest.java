package com.comisiones.util;

import com.comisiones.model.Acta;
import com.comisiones.model.Comision;
import com.comisiones.model.ComisionMiembro;
import com.comisiones.model.Miembro;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValidationUtil} covering model validation constraints.
 */
public class ValidationUtilTest {

    /** 2 years expressed in milliseconds, used for "future date" test helpers. */
    private static final long TWO_YEARS_IN_MS = 2L * 365 * 24 * 60 * 60 * 1000;

    // ─────────────────────────────────────────────
    // Miembro tests
    // ─────────────────────────────────────────────

    @Test
    void miembroValido_sinViolaciones() {
        Miembro m = new Miembro("García López, Juan", "12345678A", "juan@example.com");
        assertTrue(ValidationUtil.isValid(m));
        assertTrue(ValidationUtil.validate(m).isEmpty());
    }

    @Test
    void miembro_nombreVacio_generaError() {
        Miembro m = new Miembro("", "12345678A", "juan@example.com");
        List<String> errors = ValidationUtil.validate(m);
        assertFalse(errors.isEmpty());
    }

    @Test
    void miembro_nombreDemasiadoCorto_generaError() {
        Miembro m = new Miembro("A", "12345678A", "juan@example.com");
        List<String> errors = ValidationUtil.validate(m);
        assertFalse(errors.isEmpty());
    }

    @Test
    void miembro_dniNulo_generaError() {
        Miembro m = new Miembro("García López, Juan", null, "juan@example.com");
        List<String> errors = ValidationUtil.validate(m);
        assertTrue(errors.contains("El DNI/NIE es obligatorio"));
    }

    @Test
    void miembro_dniFormatoInvalido_generaError() {
        Miembro m = new Miembro("García López, Juan", "INVALIDO", "juan@example.com");
        List<String> errors = ValidationUtil.validate(m);
        assertTrue(errors.stream().anyMatch(e -> e.contains("DNI/NIE inválido")));
    }

    @Test
    void miembro_dniNieValido_sinViolaciones() {
        // NIE format: X1234567A
        Miembro m = new Miembro("García López, Juan", "X1234567A", "juan@example.com");
        assertTrue(ValidationUtil.isValid(m));
    }

    @Test
    void miembro_emailInvalido_generaError() {
        Miembro m = new Miembro("García López, Juan", "12345678A", "no-es-email");
        List<String> errors = ValidationUtil.validate(m);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Email inválido")));
    }

    @Test
    void miembro_emailVacio_generaError() {
        Miembro m = new Miembro("García López, Juan", "12345678A", "");
        List<String> errors = ValidationUtil.validate(m);
        assertFalse(errors.isEmpty());
    }

    @Test
    void validarConCampos_retornaMapaDeCampos() {
        Miembro m = new Miembro("", null, "no-email");
        Map<String, String> errors = ValidationUtil.validateWithFields(m);
        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("dniNif"));
    }

    // ─────────────────────────────────────────────
    // Comision tests
    // ─────────────────────────────────────────────

    @Test
    void comisionValida_sinViolaciones() {
        Comision c = new Comision("Comisión de Calidad", new Date(), null);
        c.setArea(Comision.Area.ATENCION_PRIMARIA);
        c.setTipo(Comision.Tipo.COMISION);
        assertTrue(ValidationUtil.isValid(c));
    }

    @Test
    void comision_nombreVacio_generaError() {
        Comision c = new Comision("", new Date(), null);
        c.setArea(Comision.Area.ATENCION_PRIMARIA);
        c.setTipo(Comision.Tipo.COMISION);
        List<String> errors = ValidationUtil.validate(c);
        assertTrue(errors.stream().anyMatch(e -> e.contains("nombre de la comisión es obligatorio")));
    }

    @Test
    void comision_nombreDemasiadoCorto_generaError() {
        Comision c = new Comision("AB", new Date(), null);
        c.setArea(Comision.Area.MIXTA);
        c.setTipo(Comision.Tipo.GRUPO_TRABAJO);
        List<String> errors = ValidationUtil.validate(c);
        assertFalse(errors.isEmpty());
    }

    @Test
    void comision_areaNull_generaError() {
        Comision c = new Comision("Comisión de Calidad", new Date(), null);
        c.setTipo(Comision.Tipo.COMISION);
        List<String> errors = ValidationUtil.validate(c);
        assertTrue(errors.contains("El área es obligatoria"));
    }

    @Test
    void comision_tipoNull_generaError() {
        Comision c = new Comision("Comisión de Calidad", new Date(), null);
        c.setArea(Comision.Area.ATENCION_ESPECIALIZADA);
        List<String> errors = ValidationUtil.validate(c);
        assertTrue(errors.contains("El tipo es obligatorio"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void comision_fechaFutura_generaError() {
        // Date two years in the future
        Date futuro = new Date(System.currentTimeMillis() + TWO_YEARS_IN_MS);
        Comision c = new Comision("Comisión de Calidad", futuro, null);
        c.setArea(Comision.Area.MIXTA);
        c.setTipo(Comision.Tipo.GRUPO_MEJORA);
        List<String> errors = ValidationUtil.validate(c);
        assertTrue(errors.stream().anyMatch(e -> e.contains("no puede ser futura")));
    }

    // ─────────────────────────────────────────────
    // Acta tests
    // ─────────────────────────────────────────────

    @Test
    void actaValida_sinViolaciones() {
        Acta a = new Acta();
        Comision c = new Comision("Comisión X", new Date(), null);
        a.setComision(c);
        a.setFechaReunion(LocalDate.now());
        a.setObservaciones("Sin incidencias.");
        assertTrue(ValidationUtil.isValid(a));
    }

    @Test
    void acta_comisionNula_generaError() {
        Acta a = new Acta();
        a.setFechaReunion(LocalDate.now());
        List<String> errors = ValidationUtil.validate(a);
        assertTrue(errors.contains("La comisión es obligatoria"));
    }

    @Test
    void acta_fechaNull_generaError() {
        Acta a = new Acta();
        a.setComision(new Comision());
        List<String> errors = ValidationUtil.validate(a);
        assertTrue(errors.contains("La fecha de reunión es obligatoria"));
    }

    @Test
    void acta_fechaFutura_generaError() {
        Acta a = new Acta();
        a.setComision(new Comision());
        a.setFechaReunion(LocalDate.now().plusDays(10));
        List<String> errors = ValidationUtil.validate(a);
        assertTrue(errors.stream().anyMatch(e -> e.contains("no puede ser futura")));
    }

    @Test
    void acta_observacionesDemasiadoLargas_generaError() {
        Acta a = new Acta();
        a.setComision(new Comision());
        a.setFechaReunion(LocalDate.now());
        a.setObservaciones("X".repeat(501));
        List<String> errors = ValidationUtil.validate(a);
        assertTrue(errors.stream().anyMatch(e -> e.contains("500 caracteres")));
    }

    // ─────────────────────────────────────────────
    // ComisionMiembro tests
    // ─────────────────────────────────────────────

    @Test
    void comisionMiembroValido_sinViolaciones() {
        ComisionMiembro cm = new ComisionMiembro(
            new Comision(), new Miembro(), ComisionMiembro.Cargo.PRESIDENTE, new Date()
        );
        assertTrue(ValidationUtil.isValid(cm));
    }

    @Test
    void comisionMiembro_comisionNula_generaError() {
        ComisionMiembro cm = new ComisionMiembro(null, new Miembro(), ComisionMiembro.Cargo.SECRETARIO, new Date());
        List<String> errors = ValidationUtil.validate(cm);
        assertTrue(errors.contains("La comisión es obligatoria"));
    }

    @Test
    void comisionMiembro_miembroNulo_generaError() {
        ComisionMiembro cm = new ComisionMiembro(new Comision(), null, ComisionMiembro.Cargo.SECRETARIO, new Date());
        List<String> errors = ValidationUtil.validate(cm);
        assertTrue(errors.contains("El miembro es obligatorio"));
    }

    @Test
    void comisionMiembro_cargoNulo_generaError() {
        ComisionMiembro cm = new ComisionMiembro(new Comision(), new Miembro(), null, new Date());
        List<String> errors = ValidationUtil.validate(cm);
        assertTrue(errors.contains("El cargo es obligatorio"));
    }

    @Test
    void comisionMiembro_fechaIncorporacionNula_generaError() {
        ComisionMiembro cm = new ComisionMiembro(new Comision(), new Miembro(), ComisionMiembro.Cargo.PARTICIPANTE, null);
        List<String> errors = ValidationUtil.validate(cm);
        assertTrue(errors.contains("La fecha de incorporación es obligatoria"));
    }

    @Test
    void comisionMiembro_fechaIncorporacionFutura_generaError() {
        Date futuro = new Date(System.currentTimeMillis() + TWO_YEARS_IN_MS);
        ComisionMiembro cm = new ComisionMiembro(new Comision(), new Miembro(), ComisionMiembro.Cargo.PARTICIPANTE, futuro);
        List<String> errors = ValidationUtil.validate(cm);
        assertTrue(errors.stream().anyMatch(e -> e.contains("no puede ser futura")));
    }
}

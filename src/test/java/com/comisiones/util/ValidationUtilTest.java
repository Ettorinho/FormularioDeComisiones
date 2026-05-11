package com.comisiones.util;

import com.comisiones.dto.MiembroDTO;
import com.comisiones.model.Comision;
import com.comisiones.model.Miembro;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the centralised Bean Validation utility.
 */
public class ValidationUtilTest {

    // ------------------------------------------------------------------ Miembro

    @Test
    void miembroValido_sinViolaciones() {
        Miembro m = new Miembro("García López, Ana", "12345678Z", "ana@example.com");
        assertTrue(ValidationUtil.isValid(m));
        assertNull(ValidationUtil.getErrorMessages(m));
    }

    @Test
    void miembro_nombreApellidosBlanco_generaError() {
        Miembro m = new Miembro("", "12345678Z", "ana@example.com");
        assertFalse(ValidationUtil.isValid(m));
        Set<ConstraintViolation<Miembro>> violations = ValidationUtil.validate(m);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombreApellidos")));
    }

    @Test
    void miembro_dniFormatoInvalido_generaError() {
        Miembro m = new Miembro("García López, Ana", "1234567", "ana@example.com");
        assertFalse(ValidationUtil.isValid(m));
        assertTrue(ValidationUtil.validate(m).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dniNif")));
    }

    @Test
    void miembro_emailInvalido_generaError() {
        Miembro m = new Miembro("García López, Ana", "12345678Z", "no-es-email");
        assertFalse(ValidationUtil.isValid(m));
        assertTrue(ValidationUtil.validate(m).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void miembro_dniNieFormato_valido() {
        Miembro m = new Miembro("García López, Ana", "X1234567Z", "ana@example.com");
        assertTrue(ValidationUtil.isValid(m));
    }

    // ------------------------------------------------------------------ Comision

    @Test
    void comision_valida_sinViolaciones() {
        Comision c = new Comision("Comisión de Calidad", new Date(), null);
        c.setArea(Comision.Area.MIXTA);
        c.setTipo(Comision.Tipo.COMISION);
        assertTrue(ValidationUtil.isValid(c));
    }

    @Test
    void comision_nombreBlanco_generaError() {
        Comision c = new Comision("", new Date(), null);
        c.setArea(Comision.Area.MIXTA);
        c.setTipo(Comision.Tipo.COMISION);
        assertFalse(ValidationUtil.isValid(c));
        assertTrue(ValidationUtil.validate(c).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    void comision_sinArea_generaError() {
        Comision c = new Comision("Comisión de Calidad", new Date(), null);
        c.setTipo(Comision.Tipo.COMISION);
        assertFalse(ValidationUtil.isValid(c));
        assertTrue(ValidationUtil.validate(c).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("area")));
    }

    @Test
    void comision_sinFechaConstitucion_generaError() {
        Comision c = new Comision("Comisión de Calidad", null, null);
        c.setArea(Comision.Area.MIXTA);
        c.setTipo(Comision.Tipo.COMISION);
        assertFalse(ValidationUtil.isValid(c));
        assertTrue(ValidationUtil.validate(c).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("fechaConstitucion")));
    }

    // ------------------------------------------------------------------ MiembroDTO

    @Test
    void miembroDTO_valido_sinViolaciones() {
        MiembroDTO dto = new MiembroDTO();
        dto.setDni("12345678Z");
        dto.setNombre("García López, Ana");
        dto.setEmail("ana@example.com");
        assertTrue(ValidationUtil.isValid(dto));
    }

    @Test
    void miembroDTO_dniBlanco_generaError() {
        MiembroDTO dto = new MiembroDTO();
        dto.setDni("");
        dto.setNombre("García López, Ana");
        assertFalse(ValidationUtil.isValid(dto));
    }

    @Test
    void miembroDTO_nombreBlanco_generaError() {
        MiembroDTO dto = new MiembroDTO();
        dto.setDni("12345678Z");
        dto.setNombre("");
        assertFalse(ValidationUtil.isValid(dto));
    }

    // ------------------------------------------------------------------ validateWithFields

    @Test
    void validateWithFields_devuelveMapaDeErrores() {
        Miembro m = new Miembro("", "INVALIDO", "no-email");
        Map<String, String> errors = ValidationUtil.validateWithFields(m);
        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("nombreApellidos") || errors.containsKey("dniNif") || errors.containsKey("email"));
    }

    @Test
    void validateWithFields_objetoValido_devuelveMapaVacio() {
        Miembro m = new Miembro("García López, Ana", "12345678Z", "ana@example.com");
        Map<String, String> errors = ValidationUtil.validateWithFields(m);
        assertTrue(errors.isEmpty());
    }

    // ------------------------------------------------------------------ getErrorMessages

    @Test
    void getErrorMessages_objetoInvalido_devuelveCadenaConErrores() {
        Miembro m = new Miembro("", "BAD", "");
        String msg = ValidationUtil.getErrorMessages(m);
        assertNotNull(msg);
        assertFalse(msg.isEmpty());
    }
}

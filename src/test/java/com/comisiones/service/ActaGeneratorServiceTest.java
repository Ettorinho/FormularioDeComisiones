package com.comisiones.service;

import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.model.Comision;
import com.comisiones.model.Miembro;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActaGeneratorServiceTest {

    private final ActaGeneratorService service = new ActaGeneratorService();

    @Test
    void generarPdf_repiteCabeceraYPaginaDinamicamenteCuandoResumenEsLargo() throws IOException {
        Acta acta = createActa(longText(140));
        byte[] generated = service.generarPdf(acta, createAsistencias(), 3);

        try (PDDocument document = PDDocument.load(generated)) {
            assertTrue(document.getNumberOfPages() > 1, "El resumen largo debe generar varias páginas");

            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("ACTA DE REUNIÓN:"));
            assertTrue(text.contains("MC-2_SA(P)E"));
            assertTrue(text.contains("Página 1 de " + document.getNumberOfPages()));
            assertTrue(text.contains("Página " + document.getNumberOfPages() + " de " + document.getNumberOfPages()));
            assertTrue(text.contains("ORDEN DEL DÍA"));
            assertTrue(text.contains("RESUMEN DE LA REUNIÓN"));
            assertTrue(text.contains("Revisión del Sist. Calidad"));
        }
    }

    @Test
    void generarWord_reflejaDiseñoOficialUnificado() throws IOException {
        Acta acta = createActa("Resumen breve de la reunión.");
        byte[] generated = service.generarWord(acta, createAsistencias(), 3);

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(generated))) {
            assertEquals(4, document.getTables().size(), "Debe generar cabecera, bloque principal, orden del día y resumen");
            StringBuilder builder = new StringBuilder();
            document.getTables().forEach(table -> builder.append(table.getText()).append(' '));
            String text = normalize(builder.toString());
            assertTrue(text.contains("ACTA DE REUNIÓN:"));
            assertTrue(text.contains("MC-2_SA(P)E"));
            assertTrue(text.contains("ASISTENTES"));
            assertTrue(text.contains("ORDEN DEL DÍA"));
            assertTrue(text.contains("RESUMEN DE LA REUNIÓN"));
            assertTrue(text.contains("Excusa asistencia"));
        }
    }

    private Acta createActa(String resumen) {
        Comision comision = new Comision();
        comision.setId(5L);
        comision.setNombre("Grupo de trabajo del Sistema de Archivo y Registro de Documentación");

        Acta acta = new Acta();
        acta.setId(10L);
        acta.setComision(comision);
        acta.setTitulo("Acta 10/10/2025 - Grupo de trabajo");
        acta.setFechaReunion(LocalDate.of(2025, 10, 10));
        acta.setHoraInicio("12:00");
        acta.setHoraFin("13:30");
        acta.setDuracion("1h 30 min");
        acta.setTipoReunion("CALIDAD");
        acta.setTipoReunionOtrosDetalle("");
        acta.setOrdenDia("Revisión PDCA\nDiseño de la herramienta digital");
        acta.setExcusaAsistencia("María Labarta Bellostas");
        acta.setObservaciones(resumen);
        return acta;
    }

    private List<AsistenciaActa> createAsistencias() {
        return List.of(
                createAsistencia("Adolfo Lanao Martín", "RESPONSABLE", true, null),
                createAsistencia("María Carmen Plata Gabás", "SECRETARIO", true, null),
                createAsistencia("María Labarta Bellostas", "MIEMBRO", false, "Excusa asistencia")
        );
    }

    private AsistenciaActa createAsistencia(String nombre, String cargo, boolean asistio, String justificacion) {
        Miembro miembro = new Miembro();
        miembro.setNombreApellidos(nombre);
        miembro.setDniNif("00000000T");

        AsistenciaActa asistencia = new AsistenciaActa();
        asistencia.setMiembro(miembro);
        asistencia.setCargoMiembro(cargo);
        asistencia.setAsistio(asistio);
        asistencia.setJustificacion(justificacion);
        return asistencia;
    }

    private String longText(int paragraphs) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= paragraphs; i++) {
            builder.append("Párrafo ").append(i)
                    .append(": revisión del seguimiento, acuerdos alcanzados y próximos pasos del grupo de trabajo.")
                    .append('\n');
        }
        return builder.toString();
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').replaceAll("\\s+", " ").trim();
    }
}

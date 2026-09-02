package com.comisiones.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActaGeneratorServiceTest {

    private final ActaGeneratorService service = new ActaGeneratorService();

    @Test
    void generarPlantillaVaciaWord_replicatesModeloDocStructure() throws IOException {
        byte[] generated = service.generarPlantillaVaciaWord();

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(generated))) {
            assertTrue(document.getHeaderList().isEmpty(), "La plantilla no debe usar headers Word gestionados por POI");
            assertEquals(2, document.getTables().size(), "Debe existir tabla de cabecera en cuerpo + tabla principal");

            XWPFTable headerTable = document.getTables().get(0);
            assertEquals(1, headerTable.getNumberOfRows());
            assertEquals(3, headerTable.getRow(0).getTableCells().size());
            assertEquals(STBorder.SINGLE, headerTable.getCTTbl().getTblPr().getTblBorders().getTop().getVal());
            assertTrue(normalize(headerTable.getText()).contains("SECTOR DE BARBASTRO"));
            assertTrue(normalize(headerTable.getText()).contains("ACTA DE REUNIÓN"));
            assertTrue(normalize(headerTable.getText()).contains("Revisión A"));
            assertTrue(normalize(headerTable.getText()).contains("Página 1 de 1"));
            assertFalse(headerTable.getRow(0).getCell(0).getParagraphArray(0).getRuns().isEmpty());
            assertTrue(headerTable.getRow(0).getCell(0).getParagraphArray(0).getRuns().get(0).getEmbeddedPictures().size() > 0,
                    "La celda izquierda debe contener el logo oficial");

            XWPFTable mainTable = document.getTables().get(1);
            assertEquals(7, mainTable.getNumberOfRows());
            assertEquals(STBorder.DOUBLE, mainTable.getCTTbl().getTblPr().getTblBorders().getTop().getVal());

            assertEquals(1, mainTable.getRow(0).getTableCells().size());
            assertGridSpan(mainTable.getRow(0).getCell(0), 3);
            assertNotNull(mainTable.getRow(0).getCell(0).getCTTc().getTcPr().getShd());
            assertTrue(normalize(mainTable.getRow(0).getCell(0).getText()).contains("COMISIÓN DE"));

            assertEquals(3, mainTable.getRow(1).getTableCells().size());
            assertTrue(normalize(mainTable.getRow(1).getCell(0).getText()).contains("Fecha :"));
            assertTrue(normalize(mainTable.getRow(1).getCell(1).getText()).contains("Hora inicio:"));
            assertTrue(normalize(mainTable.getRow(1).getCell(2).getText()).contains("Hora fin:"));

            assertEquals(2, mainTable.getRow(2).getTableCells().size());
            assertTrue(normalize(mainTable.getRow(2).getCell(0).getText()).contains("ASISTENTES"));
            assertTrue(normalize(mainTable.getRow(2).getCell(0).getText()).contains("(Nombre y Cargo)"));
            assertGridSpan(mainTable.getRow(2).getCell(1), 2);
            assertTrue(normalize(mainTable.getRow(2).getCell(1).getText()).contains("EXCUSAN SU ASISTENCIA"));
            assertTrue(normalize(mainTable.getRow(2).getCell(1).getText()).contains("Razón de la no asistencia"));

            assertEquals("restart", getVerticalMergeValue(mainTable.getRow(3).getCell(0)));
            assertEquals("continue", getVerticalMergeValue(mainTable.getRow(4).getCell(0)));
            assertEquals(2, mainTable.getRow(3).getTableCells().size());
            assertEquals(2, mainTable.getRow(4).getTableCells().size());
            assertGridSpan(mainTable.getRow(3).getCell(1), 2);
            assertGridSpan(mainTable.getRow(4).getCell(1), 2);

            assertEquals(1, mainTable.getRow(5).getTableCells().size());
            assertGridSpan(mainTable.getRow(5).getCell(0), 3);
            assertTrue(normalize(mainTable.getRow(5).getCell(0).getText()).contains("ORDEN DEL DIA:"));

            assertEquals(1, mainTable.getRow(6).getTableCells().size());
            assertGridSpan(mainTable.getRow(6).getCell(0), 3);
            assertTrue(normalize(mainTable.getRow(6).getCell(0).getText()).contains("RESUMEN DE LA REUNION:"));
        }
    }

    @Test
    void generarPlantillaVaciaPdf_matchesExpectedTemplateFields() throws IOException {
        byte[] generated = service.generarPlantillaVaciaPdf();

        try (PDDocument document = PDDocument.load(generated)) {
            assertEquals(1, document.getNumberOfPages());

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            assertNotNull(acroForm);

            Set<String> fieldNames = acroForm.getFields().stream()
                    .map(PDField::getFullyQualifiedName)
                    .collect(Collectors.toSet());

            assertEquals(Set.of(
                    "nombreGrupo",
                    "fecha",
                    "horaInicio",
                    "horaFin",
                    "asistentes",
                    "excusaAsistencia",
                    "ordenDelDia",
                    "resumenReunion"
            ), fieldNames);
            assertFalse(fieldNames.contains("firmaNombre"));
            assertFalse(fieldNames.contains("firmaCargo"));
            assertFalse(fieldNames.contains("firmaFechaCierre"));

            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("ACTA DE REUNIÓN"));
            assertTrue(text.contains("COMISIÓN DE"));
            assertTrue(text.contains("ASISTENTES"));
            assertTrue(text.contains("EXCUSAN SU ASISTENCIA"));
            assertTrue(text.contains("ORDEN DEL DIA:"));
            assertTrue(text.contains("RESUMEN DE LA REUNION:"));
        }
    }

    private void assertGridSpan(XWPFTableCell cell, int expectedSpan) {
        CTTcPr properties = cell.getCTTc().getTcPr();
        assertNotNull(properties);
        assertTrue(properties.isSetGridSpan());
        assertEquals(expectedSpan, properties.getGridSpan().getVal().intValue());
    }

    private String getVerticalMergeValue(XWPFTableCell cell) {
        CTTcPr properties = cell.getCTTc().getTcPr();
        assertNotNull(properties);
        assertTrue(properties.isSetVMerge());
        return properties.getVMerge().getVal().toString().toLowerCase();
    }

    private String normalize(String text) {
        return text == null ? "" : text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').replaceAll("\\s+", " ").trim();
    }
}

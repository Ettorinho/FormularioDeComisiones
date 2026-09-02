package com.comisiones.service;

import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.util.AppLogger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para generar documentos (PDF y Word) de actas.
 */
public class ActaGeneratorService {
    
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    
    // Configuración de PDF
    private static final float MARGIN = 50;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float SUBTITLE_FONT_SIZE = 14;
    private static final float NORMAL_FONT_SIZE = 12;
    
    /**
     * Convierte un LocalDate a String usando el formateador thread-safe.
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return dateFormatter.format(date);
    }
    
    /**
     * Genera un PDF con la información del acta y asistencias.
     * 
     * @param acta Acta a generar
     * @param asistencias Lista de asistencias del acta
     * @param numeroActaEnComision Número secuencial del acta dentro de su comisión
     * @return Contenido del PDF como array de bytes
     * @throws IOException Si hay un error al generar el PDF
     */
    public byte[] generarPdf(Acta acta, List<AsistenciaActa> asistencias, int numeroActaEnComision) throws IOException {
        AppLogger.debug("Generando PDF para acta ID: " + acta.getId());
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;
                
                // Título
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("ACTA DE REUNIÓN #" + numeroActaEnComision);
                contentStream.endText();
                yPosition -= 30;
                
                // Información de la comisión
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Comisión:");
                contentStream.endText();
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(acta.getComision().getNombre());
                contentStream.endText();
                yPosition -= 25;
                
                // Fecha de reunión
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Fecha de Reunión:");
                contentStream.endText();
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(formatDate(acta.getFechaReunion()));
                contentStream.endText();
                yPosition -= 25;
                
                // Observaciones
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Observaciones:");
                contentStream.endText();
                yPosition -= 20;
                
                String observaciones = acta.getObservaciones();
                if (observaciones == null || observaciones.trim().isEmpty()) {
                    observaciones = "Sin observaciones";
                }
                
                // Dividir observaciones en líneas si son muy largas
                String[] obsLines = splitTextIntoLines(observaciones, 80);
                for (String line : obsLines) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= 15;
                }
                yPosition -= 15;
                
                // Tabla de asistencias
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Asistencias:");
                contentStream.endText();
                yPosition -= 25;
                
                // Encabezados de tabla
                float tableTop = yPosition;
                float tableWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
                float col1Width = tableWidth * 0.35f;
                float col2Width = tableWidth * 0.20f;
                float col3Width = tableWidth * 0.15f;
                float col4Width = tableWidth * 0.30f;
                float rowHeight = 20f;
                
                // Encabezado
                contentStream.setLineWidth(1f);
                contentStream.addRect(MARGIN, yPosition - rowHeight, col1Width, rowHeight);
                contentStream.addRect(MARGIN + col1Width, yPosition - rowHeight, col2Width, rowHeight);
                contentStream.addRect(MARGIN + col1Width + col2Width, yPosition - rowHeight, col3Width, rowHeight);
                contentStream.addRect(MARGIN + col1Width + col2Width + col3Width, yPosition - rowHeight, col4Width, rowHeight);
                contentStream.stroke();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN + 5, yPosition - 15);
                contentStream.showText("Nombre y Apellidos");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN + col1Width + 5, yPosition - 15);
                contentStream.showText("DNI/NIF");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN + col1Width + col2Width + 5, yPosition - 15);
                contentStream.showText("Asistencia");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                contentStream.newLineAtOffset(MARGIN + col1Width + col2Width + col3Width + 5, yPosition - 15);
                contentStream.showText("Justificación");
                contentStream.endText();
                
                yPosition -= rowHeight;
                
                // Filas de datos
                if (asistencias != null) {
                    for (AsistenciaActa asistencia : asistencias) {
                        contentStream.addRect(MARGIN, yPosition - rowHeight, col1Width, rowHeight);
                        contentStream.addRect(MARGIN + col1Width, yPosition - rowHeight, col2Width, rowHeight);
                        contentStream.addRect(MARGIN + col1Width + col2Width, yPosition - rowHeight, col3Width, rowHeight);
                        contentStream.addRect(MARGIN + col1Width + col2Width + col3Width, yPosition - rowHeight, col4Width, rowHeight);
                        contentStream.stroke();
                        
                        // Nombre
                        String nombre = asistencia.getMiembro().getNombreApellidos();
                        if (nombre.length() > 30) {
                            nombre = nombre.substring(0, 27) + "...";
                        }
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                        contentStream.newLineAtOffset(MARGIN + 5, yPosition - 15);
                        contentStream.showText(nombre);
                        contentStream.endText();
                        
                        // DNI
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                        contentStream.newLineAtOffset(MARGIN + col1Width + 5, yPosition - 15);
                        contentStream.showText(asistencia.getMiembro().getDniNif());
                        contentStream.endText();
                        
                        // Asistencia
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                        contentStream.newLineAtOffset(MARGIN + col1Width + col2Width + 5, yPosition - 15);
                        contentStream.showText(asistencia.isAsistio() ? "Sí" : "No");
                        contentStream.endText();
                        
                        // Justificación
                        String justificacion = asistencia.getJustificacion();
                        if (justificacion != null && !justificacion.isEmpty()) {
                            if (justificacion.length() > 25) {
                                justificacion = justificacion.substring(0, 22) + "...";
                            }
                            contentStream.beginText();
                            contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                            contentStream.newLineAtOffset(MARGIN + col1Width + col2Width + col3Width + 5, yPosition - 15);
                            contentStream.showText(justificacion);
                            contentStream.endText();
                        }
                        
                        yPosition -= rowHeight;
                    }
                }
                
                // Pie de página con fecha de generación
                yPosition -= 30;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(MARGIN, 50);
                contentStream.showText("Documento generado el: " + formatDate(LocalDate.now()));
                contentStream.endText();
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            AppLogger.debug("PDF generado correctamente");
            return baos.toByteArray();
        }
    }
    
    /**
     * Genera un documento Word con la información del acta y asistencias.
     * 
     * @param acta Acta a generar
     * @param asistencias Lista de asistencias del acta
     * @param numeroActaEnComision Número secuencial del acta dentro de su comisión
     * @return Contenido del documento Word como array de bytes
     * @throws IOException Si hay un error al generar el documento
     */
    public byte[] generarWord(Acta acta, List<AsistenciaActa> asistencias, int numeroActaEnComision) throws IOException {
        AppLogger.debug("Generando Word para acta ID: " + acta.getId());
        
        try (XWPFDocument document = new XWPFDocument()) {
            // Título
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("ACTA DE REUNIÓN #" + numeroActaEnComision);
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.addBreak();
            
            // Información de la comisión
            XWPFParagraph comisionParagraph = document.createParagraph();
            XWPFRun comisionLabelRun = comisionParagraph.createRun();
            comisionLabelRun.setText("Comisión: ");
            comisionLabelRun.setBold(true);
            comisionLabelRun.setFontSize(14);
            
            XWPFRun comisionValueRun = comisionParagraph.createRun();
            comisionValueRun.setText(acta.getComision().getNombre());
            comisionValueRun.setFontSize(12);
            
            // Fecha de reunión
            XWPFParagraph fechaParagraph = document.createParagraph();
            XWPFRun fechaLabelRun = fechaParagraph.createRun();
            fechaLabelRun.setText("Fecha de Reunión: ");
            fechaLabelRun.setBold(true);
            fechaLabelRun.setFontSize(14);
            
            XWPFRun fechaValueRun = fechaParagraph.createRun();
            fechaValueRun.setText(formatDate(acta.getFechaReunion()));
            fechaValueRun.setFontSize(12);
            
            // Observaciones
            XWPFParagraph observacionesParagraph = document.createParagraph();
            XWPFRun observacionesLabelRun = observacionesParagraph.createRun();
            observacionesLabelRun.setText("Observaciones: ");
            observacionesLabelRun.setBold(true);
            observacionesLabelRun.setFontSize(14);
            observacionesLabelRun.addBreak();
            
            XWPFRun observacionesValueRun = observacionesParagraph.createRun();
            String observaciones = acta.getObservaciones();
            if (observaciones == null || observaciones.trim().isEmpty()) {
                observaciones = "Sin observaciones";
            }
            observacionesValueRun.setText(observaciones);
            observacionesValueRun.setFontSize(12);
            observacionesValueRun.addBreak();
            observacionesValueRun.addBreak();
            
            // Tabla de asistencias
            XWPFParagraph asistenciasTitleParagraph = document.createParagraph();
            XWPFRun asistenciasTitleRun = asistenciasTitleParagraph.createRun();
            asistenciasTitleRun.setText("Asistencias:");
            asistenciasTitleRun.setBold(true);
            asistenciasTitleRun.setFontSize(14);
            
            // Crear tabla
            int numRows = (asistencias != null ? asistencias.size() : 0) + 1; // +1 para encabezado
            XWPFTable table = document.createTable(numRows, 4);
            table.setWidth("100%");
            
            // Encabezados
            XWPFTableRow headerRow = table.getRow(0);
            setTableCellText(headerRow.getCell(0), "Nombre y Apellidos", true);
            setTableCellText(headerRow.getCell(1), "DNI/NIF", true);
            setTableCellText(headerRow.getCell(2), "Asistencia", true);
            setTableCellText(headerRow.getCell(3), "Justificación", true);
            
            // Datos
            if (asistencias != null) {
                int rowIndex = 1;
                for (AsistenciaActa asistencia : asistencias) {
                    XWPFTableRow row = table.getRow(rowIndex);
                    
                    String nombre = asistencia.getMiembro().getNombreApellidos();
                    setTableCellText(row.getCell(0), nombre, false);
                    setTableCellText(row.getCell(1), asistencia.getMiembro().getDniNif(), false);
                    setTableCellText(row.getCell(2), asistencia.isAsistio() ? "Sí" : "No", false);
                    
                    String justificacion = asistencia.getJustificacion();
                    setTableCellText(row.getCell(3), justificacion != null ? justificacion : "", false);
                    
                    rowIndex++;
                }
            }
            
            // Pie de página
            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun footerRun = footerParagraph.createRun();
            footerRun.addBreak();
            footerRun.addBreak();
            footerRun.setText("Documento generado el: " + formatDate(LocalDate.now()));
            footerRun.setFontSize(10);
            footerRun.setItalic(true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            AppLogger.debug("Word generado correctamente");
            return baos.toByteArray();
        }
    }
    
    /**
     * Genera un PDF rellenable (AcroForm) de 3 páginas A4 con el layout de tablas/recuadros
     * de la plantilla oficial de acta de reunión (MC-2_SA(P)E).
     *
     * <p>Cada página incluye el encabezado de 3 columnas. La página 1 contiene el bloque
     * principal (ASISTENTES, TIPO REUNIÓN, ORDEN DEL DÍA, RESUMEN). Las páginas 2 y 3
     * son continuación del RESUMEN. La página 3 incluye también el bloque de firma al final.</p>
     *
     * @return Contenido del PDF como array de bytes
     * @throws IOException Si hay un error al generar el PDF
     */
    public byte[] generarPlantillaVaciaPdf() throws IOException {
        AppLogger.debug("Generando plantilla de acta vacía en PDF a partir del modelo oficial");

        try (PDDocument document = new PDDocument()) {
            PDPage page1 = new PDPage(PDRectangle.A4);
            document.addPage(page1);

            final float pageWidth = PDRectangle.A4.getWidth();
            final float pageHeight = PDRectangle.A4.getHeight();
            final float leftMargin = 40f;
            final float rightMargin = 40f;
            final float topMargin = 38f;
            final float bottomMargin = 40f;
            final float contentWidth = pageWidth - leftMargin - rightMargin;
            final float contentHeight = pageHeight - topMargin - bottomMargin;

            final float headerHeight = 71f;
            final float headerTop = pageHeight - topMargin;
            final float headerBottom = headerTop - headerHeight;
            final float headerLeftWidth = contentWidth * (3001f / 10366f);
            final float headerCenterWidth = contentWidth * (5137f / 10366f);
            final float headerRightWidth = contentWidth - headerLeftWidth - headerCenterWidth;

            final float bodyTop = headerBottom - 8f;
            final float bodyBottom = bottomMargin;
            final float bodyHeight = bodyTop - bodyBottom;
            final float bodyCol1Width = contentWidth * (4930f / 9790f);
            final float bodyCol2Width = contentWidth * (2553f / 9790f);
            final float bodyCol3Width = contentWidth - bodyCol1Width - bodyCol2Width;

            final float row1Height = 36f;
            final float row2Height = 28f;
            final float row3Height = 42f;
            final float row4Height = 82f;
            final float row5Height = 54f;
            final float row6Height = 110f;
            final float row7Height = bodyHeight - row1Height - row2Height - row3Height - row4Height - row5Height - row6Height;

            final float row1Bottom = bodyTop - row1Height;
            final float row2Bottom = row1Bottom - row2Height;
            final float row3Bottom = row2Bottom - row3Height;
            final float row4Bottom = row3Bottom - row4Height;
            final float row5Bottom = row4Bottom - row5Height;
            final float row6Bottom = row5Bottom - row6Height;

            // Cargar logo desde recursos de classpath
            PDImageXObject logoImage = null;
            try (InputStream logoStream = getClass().getResourceAsStream("/images/logo_salud.png")) {
                if (logoStream != null) {
                    byte[] logoBytes = logoStream.readAllBytes();
                    logoImage = PDImageXObject.createFromByteArray(document, logoBytes, "logo_salud");
                }
            } catch (Exception e) {
                AppLogger.debug("Logo no disponible, se mostrará texto placeholder: " + e.getMessage());
            }

            // Configurar AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
            acroForm.setNeedAppearances(true);

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 1 — Encabezado
            // ════════════════════════════════════════════════════════════════════
            try (PDPageContentStream cs = new PDPageContentStream(document, page1)) {
                drawPageHeaderStatic(cs, document, page1, logoImage,
                        leftMargin, contentWidth, headerTop, headerBottom,
                        headerLeftWidth, headerCenterWidth, headerRightWidth, headerHeight, 1, 1);
            }

            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.setNonStrokingColor(229, 229, 229);
                cs.addRect(leftMargin, row1Bottom, contentWidth, row1Height);
                cs.fill();
                cs.setNonStrokingColor(0, 0, 0);

                cs.addRect(leftMargin, bodyBottom, contentWidth, bodyHeight);
                cs.addRect(leftMargin + 2f, bodyBottom + 2f, contentWidth - 4f, bodyHeight - 4f);
                cs.moveTo(leftMargin, row1Bottom);
                cs.lineTo(leftMargin + contentWidth, row1Bottom);
                cs.moveTo(leftMargin, row2Bottom);
                cs.lineTo(leftMargin + contentWidth, row2Bottom);
                cs.moveTo(leftMargin, row3Bottom);
                cs.lineTo(leftMargin + contentWidth, row3Bottom);
                cs.moveTo(leftMargin + bodyCol1Width, row4Bottom);
                cs.lineTo(leftMargin + contentWidth, row4Bottom);
                cs.moveTo(leftMargin, row5Bottom);
                cs.lineTo(leftMargin + contentWidth, row5Bottom);
                cs.moveTo(leftMargin, row6Bottom);
                cs.lineTo(leftMargin + contentWidth, row6Bottom);

                cs.moveTo(leftMargin + bodyCol1Width, row1Bottom);
                cs.lineTo(leftMargin + bodyCol1Width, row2Bottom);
                cs.moveTo(leftMargin + bodyCol1Width + bodyCol2Width, row1Bottom);
                cs.lineTo(leftMargin + bodyCol1Width + bodyCol2Width, row2Bottom);
                cs.moveTo(leftMargin + bodyCol1Width, row2Bottom);
                cs.lineTo(leftMargin + bodyCol1Width, row5Bottom);
                cs.stroke();

                drawCenteredText(cs, "COMISIÓN DE", leftMargin, bodyTop - 21f, contentWidth, PDType1Font.HELVETICA_BOLD, 12f);

                drawLeftText(cs, "Fecha :", leftMargin + 6f, row1Bottom - 17f, PDType1Font.HELVETICA_BOLD, 10f);
                drawLeftText(cs, "Hora inicio:", leftMargin + bodyCol1Width + 6f, row1Bottom - 17f, PDType1Font.HELVETICA_BOLD, 10f);
                drawLeftText(cs, "Hora fin:", leftMargin + bodyCol1Width + bodyCol2Width + 6f, row1Bottom - 17f, PDType1Font.HELVETICA_BOLD, 10f);

                drawCenteredText(cs, "ASISTENTES", leftMargin, row2Bottom - 16f, bodyCol1Width, PDType1Font.HELVETICA_BOLD, 10f);
                drawCenteredText(cs, "(Nombre y Cargo)", leftMargin, row2Bottom - 29f, bodyCol1Width, PDType1Font.HELVETICA_OBLIQUE, 9f);
                drawCenteredText(cs, "EXCUSAN SU ASISTENCIA", leftMargin + bodyCol1Width, row2Bottom - 16f,
                        bodyCol2Width + bodyCol3Width, PDType1Font.HELVETICA_BOLD, 10f);
                drawCenteredText(cs, "(Nombre, Cargo y Razón de la no asistencia)", leftMargin + bodyCol1Width,
                        row2Bottom - 29f, bodyCol2Width + bodyCol3Width, PDType1Font.HELVETICA_OBLIQUE, 8f);

                drawLeftText(cs, "ORDEN DEL DIA:", leftMargin + 6f, row5Bottom - 16f, PDType1Font.HELVETICA_BOLD, 10f);
                drawLeftText(cs, "RESUMEN DE LA REUNION:", leftMargin + 6f, row6Bottom - 16f, PDType1Font.HELVETICA_BOLD, 10f);
                cs.stroke();
            }

            addTextField(acroForm, page1, "nombreGrupo", false,
                    leftMargin + 8f, row1Bottom + 5f, contentWidth - 16f, 12f);
            addTextField(acroForm, page1, "fecha", false,
                    leftMargin + 48f, row2Bottom + 5f, bodyCol1Width - 56f, 12f);
            addTextField(acroForm, page1, "horaInicio", false,
                    leftMargin + bodyCol1Width + 70f, row2Bottom + 5f, bodyCol2Width - 78f, 12f);
            addTextField(acroForm, page1, "horaFin", false,
                    leftMargin + bodyCol1Width + bodyCol2Width + 56f, row2Bottom + 5f, bodyCol3Width - 64f, 12f);
            addTextField(acroForm, page1, "asistentes", true,
                    leftMargin + 3f, row5Bottom + 3f, bodyCol1Width - 6f, row3Bottom - row5Bottom - 6f);
            addTextField(acroForm, page1, "excusaAsistencia", true,
                    leftMargin + bodyCol1Width + 3f, row4Bottom + 3f, bodyCol2Width + bodyCol3Width - 6f, row3Bottom - row4Bottom - 6f);
            addTextField(acroForm, page1, "ordenDelDia", true,
                    leftMargin + 3f, row6Bottom + 3f, contentWidth - 6f, row5Bottom - row6Bottom - 20f);
            addScrollableTextField(acroForm, page1, "resumenReunion",
                    leftMargin + 3f, bodyBottom + 3f, contentWidth - 6f, row6Bottom - bodyBottom - 20f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            AppLogger.debug("Plantilla de acta vacía PDF generada correctamente");
            return baos.toByteArray();
        }
    }

    /**
     * Genera una plantilla de acta vacía en Word (.docx) con la misma estructura visual
     * que la plantilla PDF en blanco.
     */
    public byte[] generarPlantillaVaciaWord() throws IOException {
        AppLogger.debug("Generando plantilla de acta vacía en Word");

        try (XWPFDocument document = new XWPFDocument()) {
            configureBlankTemplateSection(document);

            XWPFTable headerTable = document.createTable(1, 3);
            configureFixedWidthTable(headerTable, false, 10366, 3001, 5137, 2228);
            headerTable.getRow(0).setHeight(1422);
            buildBlankTemplateHeaderRow(headerTable.getRow(0));

            XWPFTable mainTable = document.createTable(7, 3);
            configureFixedWidthTable(mainTable, true, 9790, 4930, 2553, 2307);
            buildBlankTemplateMainTable(mainTable);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            AppLogger.debug("Plantilla de acta vacía Word generada correctamente");
            return baos.toByteArray();
        }
    }

    /**
     * Dibuja el encabezado estático de 3 columnas con bordes en una página.
     * Columna 1: logo (o texto placeholder si no hay imagen). Columna 2: título ACTA DE REUNIÓN.
     * Columna 3: referencia, revisión, número de página (campo editable).
     */
    private void drawPageHeaderStatic(PDPageContentStream cs, PDDocument document, PDPage page,
            PDImageXObject logoImage,
            float m, float cw, float hdrTop, float hdrBot,
            float logoW, float col2W, float col3W, float hdrH, int pageNumber, int totalPages) throws IOException {
        cs.setLineWidth(0.8f);
        cs.addRect(m, hdrBot, cw, hdrH);
        cs.moveTo(m + logoW, hdrBot);
        cs.lineTo(m + logoW, hdrTop);
        cs.moveTo(m + logoW + col2W, hdrBot);
        cs.lineTo(m + logoW + col2W, hdrTop);
        cs.stroke();

        // Logo en columna izquierda
        if (logoImage != null) {
            float imgNatW = logoImage.getWidth();
            float imgNatH = logoImage.getHeight();
            float cellW = logoW - 12f;
            float cellH = hdrH - 28f;
            float scale = Math.min(cellW / imgNatW, cellH / imgNatH);
            float drawW = imgNatW * scale;
            float drawH = imgNatH * scale;
            float imgX = m + 6f + (cellW - drawW) / 2f;
            float imgY = hdrBot + 18f + (cellH - drawH) / 2f;
            cs.drawImage(logoImage, imgX, imgY, drawW, drawH);
        }
        cs.setNonStrokingColor(0, 153, 153);
        drawCenteredText(cs, "SECTOR DE BARBASTRO", m, hdrBot + 8f, logoW, PDType1Font.HELVETICA_BOLD, 8f);
        cs.setNonStrokingColor(0, 0, 0);

        // Título centrado
        drawCenteredText(cs, "ACTA DE REUNI\u00D3N", m + logoW, hdrBot + hdrH / 2f - 4f,
                col2W, PDType1Font.HELVETICA_BOLD, 12f);

        // Columna derecha: revisión y página
        drawCenteredText(cs, "Revisi\u00F3n A", m + logoW + col2W, hdrTop - 25f,
                col3W, PDType1Font.HELVETICA_BOLD, 9f);
        drawCenteredText(cs, "P\u00E1gina " + pageNumber + " de " + totalPages, m + logoW + col2W,
                hdrTop - 43f, col3W, PDType1Font.HELVETICA_BOLD, 9f);
    }

    /**
     * Añade un campo de texto rellenable (PDTextField) al formulario AcroForm.
     *
     * @param acroForm  El formulario AcroForm del documento
     * @param page      La página donde aparece el campo
     * @param fieldName Nombre único del campo
     * @param multiline Si true, el campo acepta múltiples líneas
     * @param x         Coordenada x (origen inferior izquierdo)
     * @param y         Coordenada y del borde inferior del campo
     * @param w         Anchura del campo
     * @param h         Altura del campo
     */
    private void addTextField(PDAcroForm acroForm, PDPage page, String fieldName,
            boolean multiline, float x, float y, float w, float h) throws IOException {
        PDTextField field = new PDTextField(acroForm);
        field.setPartialName(fieldName);
        if (multiline) {
            field.setMultiline(true);
        }
        PDAnnotationWidget widget = field.getWidgets().get(0);
        widget.setRectangle(new PDRectangle(x, y, w, h));
        widget.setPage(page);
        page.getAnnotations().add(widget);
        acroForm.getFields().add(field);
    }

    /**
     * Añade un campo de texto multilínea con scroll interno habilitado.
     * El usuario puede escribir texto extenso sin que el campo necesite expandirse.
     */
    private void addScrollableTextField(PDAcroForm acroForm, PDPage page, String fieldName,
            float x, float y, float w, float h) throws IOException {
        PDTextField field = new PDTextField(acroForm);
        field.setPartialName(fieldName);
        field.setMultiline(true);
        field.setDoNotScroll(false);
        PDAnnotationWidget widget = field.getWidgets().get(0);
        widget.setRectangle(new PDRectangle(x, y, w, h));
        widget.setPage(page);
        page.getAnnotations().add(widget);
        acroForm.getFields().add(field);
    }

    /**
     *
     * @param acroForm  El formulario AcroForm del documento
     * @param page      La página donde aparece el checkbox
     * @param fieldName Nombre único del campo
     * @param x         Coordenada x (origen inferior izquierdo del cuadrado)
     * @param y         Coordenada y del borde inferior del cuadrado
     * @param size      Tamaño (anchura y altura) del cuadrado del checkbox
     */
    private void addCheckBox(PDAcroForm acroForm, PDPage page, String fieldName,
            float x, float y, float size) throws IOException {
        PDCheckBox checkBox = new PDCheckBox(acroForm);
        checkBox.setPartialName(fieldName);
        PDAnnotationWidget widget = checkBox.getWidgets().get(0);
        widget.setRectangle(new PDRectangle(x, y, size, size));
        widget.setPage(page);
        page.getAnnotations().add(widget);
        acroForm.getFields().add(checkBox);
        checkBox.unCheck();
    }

    // ── Helpers para dibujo PDF ───────────────────────────────────────────────────

    private void drawHorizontalLine(PDPageContentStream cs, float x, float y, float width) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y);
        cs.lineTo(x + width, y);
        cs.stroke();
    }

    private void drawUnderline(PDPageContentStream cs, float x, float y, float width) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y);
        cs.lineTo(x + width, y);
        cs.stroke();
    }

    private void drawCheckbox(PDPageContentStream cs, float x, float y, String label) throws IOException {
        float size = 10f;
        cs.setLineWidth(0.8f);
        cs.addRect(x, y - 1, size, size);
        cs.stroke();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
        cs.newLineAtOffset(x + size + 4, y + 1);
        cs.showText(label);
        cs.endText();
    }

    private void drawTableRow(PDPageContentStream cs, float x, float y,
                              float col1Width, float col2Width, float rowHeight,
                              String text1, String text2, boolean bold) throws IOException {
        cs.setLineWidth(0.5f);
        cs.addRect(x, y - rowHeight, col1Width, rowHeight);
        cs.addRect(x + col1Width, y - rowHeight, col2Width, rowHeight);
        cs.stroke();
        if (!text1.isEmpty() || !text2.isEmpty()) {
            PDType1Font font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
            cs.beginText();
            cs.setFont(font, NORMAL_FONT_SIZE);
            cs.newLineAtOffset(x + 4, y - rowHeight + 4);
            cs.showText(text1);
            cs.endText();
            cs.beginText();
            cs.setFont(font, NORMAL_FONT_SIZE);
            cs.newLineAtOffset(x + col1Width + 4, y - rowHeight + 4);
            cs.showText(text2);
            cs.endText();
        }
    }

    private void drawLeftText(PDPageContentStream cs, String text, float x, float y,
                              PDType1Font font, float fontSize) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawCenteredText(PDPageContentStream cs, String text, float x, float y,
                                  float width, PDType1Font font, float fontSize) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        drawLeftText(cs, text, x + Math.max(0f, (width - textWidth) / 2f), y, font, fontSize);
    }

    /**
     * Divide un texto en líneas de longitud máxima.
     */
    private String[] splitTextIntoLines(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return new String[]{"Sin observaciones"};
        }
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        List<String> lines = new java.util.ArrayList<>();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxLength) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    /**
     * Establece el texto de una celda de tabla con formato.
     */
    private void setTableCellText(XWPFTableCell cell, String text, boolean bold) {
        clearCell(cell);
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        if (bold) {
            run.setBold(true);
        }
    }

    private void addBlankLines(XWPFTableCell cell, int lines) {
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        for (int i = 0; i < lines; i++) {
            run.addBreak();
        }
    }

    private void clearCell(XWPFTableCell cell) {
        int paragraphCount = cell.getParagraphs().size();
        for (int i = paragraphCount - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
    }

    private void configureBlankTemplateSection(XWPFDocument document) {
        CTSectPr section = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();

        CTPageSz pageSize = section.isSetPgSz() ? section.getPgSz() : section.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906));
        pageSize.setH(BigInteger.valueOf(16838));
        pageSize.setOrient(STPageOrientation.PORTRAIT);

        CTPageMar pageMargins = section.isSetPgMar() ? section.getPgMar() : section.addNewPgMar();
        pageMargins.setTop(BigInteger.valueOf(720));
        pageMargins.setBottom(BigInteger.valueOf(720));
        pageMargins.setLeft(BigInteger.valueOf(720));
        pageMargins.setRight(BigInteger.valueOf(720));
        pageMargins.setHeader(BigInteger.valueOf(360));
        pageMargins.setFooter(BigInteger.valueOf(360));
        pageMargins.setGutter(BigInteger.ZERO);
    }

    private void buildBlankTemplateHeaderRow(XWPFTableRow row) throws IOException {
        XWPFTableCell logoCell = row.getCell(0);
        clearCell(logoCell);
        logoCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph logoParagraph = createCellParagraph(logoCell, ParagraphAlignment.CENTER, 0, 0);
        XWPFRun logoRun = logoParagraph.createRun();
        try (InputStream logoStream = getClass().getResourceAsStream("/images/logo_salud.png")) {
            if (logoStream != null) {
                logoRun.addPicture(logoStream, XWPFDocument.PICTURE_TYPE_PNG,
                        "logo_salud.png", Units.toEMU(101), Units.toEMU(51));
            }
        } catch (Exception e) {
            AppLogger.debug("No se pudo insertar logo en Word: " + e.getMessage());
        }
        XWPFParagraph sectorParagraph = createCellParagraph(logoCell, ParagraphAlignment.CENTER, 0, 0);
        XWPFRun sectorRun = sectorParagraph.createRun();
        sectorRun.setText("SECTOR DE BARBASTRO");
        sectorRun.setBold(true);
        sectorRun.setColor("009999");
        sectorRun.setFontFamily("Verdana");
        sectorRun.setFontSize(8);

        XWPFTableCell titleCell = row.getCell(1);
        clearCell(titleCell);
        titleCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph titleParagraph = createCellParagraph(titleCell, ParagraphAlignment.CENTER, 0, 0);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("ACTA DE REUNIÓN");
        titleRun.setBold(true);
        titleRun.setFontFamily("Tahoma");
        titleRun.setFontSize(12);

        XWPFTableCell rightCell = row.getCell(2);
        clearCell(rightCell);
        rightCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph revisionParagraph = createCellParagraph(rightCell, ParagraphAlignment.CENTER, 0, 0);
        XWPFRun revisionRun = revisionParagraph.createRun();
        revisionRun.setText("Revisión A");
        revisionRun.setBold(true);
        revisionRun.setFontSize(9);

        XWPFParagraph pageParagraph = createCellParagraph(rightCell, ParagraphAlignment.CENTER, 0, 0);
        XWPFRun pageRun = pageParagraph.createRun();
        pageRun.setText("Página 1 de 1");
        pageRun.setBold(true);
        pageRun.setFontSize(9);
    }

    private void buildBlankTemplateMainTable(XWPFTable table) {
        XWPFTableRow row1 = table.getRow(0);
        row1.setHeight(620);
        mergeCellsHorizontally(row1, 0, 3);
        fillCell(row1.getCell(0), "COMISIÓN DE", true, "Arial", 12, null, ParagraphAlignment.CENTER, 0, 0);
        row1.getCell(0).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        setCellShading(row1.getCell(0), "E5E5E5");

        XWPFTableRow row2 = table.getRow(1);
        row2.setHeight(560);
        fillCell(row2.getCell(0), "Fecha :", true, "Arial", 10, null, ParagraphAlignment.LEFT, 0, 0);
        fillCell(row2.getCell(1), "Hora inicio:", true, "Arial", 10, null, ParagraphAlignment.LEFT, 0, 0);
        fillCell(row2.getCell(2), "Hora fin:", true, "Arial", 10, null, ParagraphAlignment.LEFT, 0, 0);

        XWPFTableRow row3 = table.getRow(2);
        row3.setHeight(780);
        fillCell(row3.getCell(0), "ASISTENTES", false, "Arial", 10, null, ParagraphAlignment.CENTER, 0, 0);
        appendCellParagraph(row3.getCell(0), "(Nombre y Cargo)", false, true, "Arial", 10, null, ParagraphAlignment.CENTER, 0, 0);
        mergeCellsHorizontally(row3, 1, 2);
        fillCell(row3.getCell(1), "EXCUSAN SU ASISTENCIA", false, "Arial", 10, null, ParagraphAlignment.CENTER, 0, 0);
        appendCellParagraph(row3.getCell(1), "(Nombre, Cargo y Razón de la no asistencia)",
                false, true, "Arial", 10, null, ParagraphAlignment.CENTER, 0, 0);

        XWPFTableRow row4 = table.getRow(3);
        row4.setHeight(1465);
        setVerticalMerge(row4.getCell(0), STMerge.RESTART);
        fillCell(row4.getCell(0), "", false, "Arial", 10, null, ParagraphAlignment.BOTH, 0, 0);
        mergeCellsHorizontally(row4, 1, 2);
        fillCell(row4.getCell(1), "", false, "Arial", 10, null, ParagraphAlignment.BOTH, 0, 0);

        XWPFTableRow row5 = table.getRow(4);
        row5.setHeight(950);
        setVerticalMerge(row5.getCell(0), STMerge.CONTINUE);
        fillCell(row5.getCell(0), "", false, "Arial", 10, null, ParagraphAlignment.BOTH, 0, 0);
        mergeCellsHorizontally(row5, 1, 2);
        fillCell(row5.getCell(1), "", false, "Arial", 10, null, ParagraphAlignment.BOTH, 0, 0);

        XWPFTableRow row6 = table.getRow(5);
        row6.setHeight(2268);
        mergeCellsHorizontally(row6, 0, 3);
        fillCell(row6.getCell(0), "ORDEN DEL DIA:", true, "Arial", 10, null, ParagraphAlignment.LEFT, 0, 0);

        XWPFTableRow row7 = table.getRow(6);
        row7.setHeight(5209);
        mergeCellsHorizontally(row7, 0, 3);
        fillCell(row7.getCell(0), "RESUMEN DE LA REUNION:", true, "Arial", 10, null, ParagraphAlignment.BOTH, 60, 0);
    }

    private void configureFixedWidthTable(XWPFTable table, boolean doubleOuterBorder, int totalWidth, int... columnWidths) {
        table.setTableAlignment(TableRowAlign.CENTER);
        table.setCellMargins(0, 70, 0, 70);

        CTTbl ctTable = table.getCTTbl();
        CTTblPr tableProperties = ctTable.getTblPr() != null ? ctTable.getTblPr() : ctTable.addNewTblPr();

        CTTblWidth width = tableProperties.isSetTblW() ? tableProperties.getTblW() : tableProperties.addNewTblW();
        width.setType(STTblWidth.DXA);
        width.setW(BigInteger.valueOf(totalWidth));

        CTTblLayoutType layout = tableProperties.isSetTblLayout() ? tableProperties.getTblLayout() : tableProperties.addNewTblLayout();
        layout.setType(STTblLayoutType.FIXED);

        CTTblGrid grid = ctTable.getTblGrid() != null ? ctTable.getTblGrid() : ctTable.addNewTblGrid();
        while (grid.sizeOfGridColArray() > 0) {
            grid.removeGridCol(0);
        }
        for (int columnWidth : columnWidths) {
            CTTblGridCol column = grid.addNewGridCol();
            column.setW(BigInteger.valueOf(columnWidth));
        }

        applyTableBorders(table, doubleOuterBorder);
        for (XWPFTableRow row : table.getRows()) {
            for (int i = 0; i < row.getTableCells().size() && i < columnWidths.length; i++) {
                setCellWidth(row.getCell(i), columnWidths[i]);
            }
        }
    }

    private void applyTableBorders(XWPFTable table, boolean doubleOuterBorder) {
        CTTblPr tableProperties = table.getCTTbl().getTblPr() != null
                ? table.getCTTbl().getTblPr()
                : table.getCTTbl().addNewTblPr();
        CTTblBorders borders = tableProperties.isSetTblBorders()
                ? tableProperties.getTblBorders()
                : tableProperties.addNewTblBorders();

        STBorder.Enum outerStyle = doubleOuterBorder ? STBorder.DOUBLE : STBorder.SINGLE;
        setBorder(borders.isSetTop() ? borders.getTop() : borders.addNewTop(), outerStyle);
        setBorder(borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom(), outerStyle);
        setBorder(borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft(), outerStyle);
        setBorder(borders.isSetRight() ? borders.getRight() : borders.addNewRight(), outerStyle);
        setBorder(borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH());
        setBorder(borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV());
    }

    private void setBorder(CTBorder border) {
        setBorder(border, STBorder.SINGLE);
    }

    private void setBorder(CTBorder border, STBorder.Enum style) {
        border.setVal(style);
        border.setSz(BigInteger.valueOf(STBorder.DOUBLE.equals(style) ? 8 : 4));
        border.setColor("000000");
        border.setSpace(BigInteger.ZERO);
    }

    private void setCellShading(XWPFTableCell cell, String fillColor) {
        CTTcPr properties = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTShd shading = properties.isSetShd() ? properties.getShd() : properties.addNewShd();
        shading.setFill(fillColor);
    }

    private void setCellWidth(XWPFTableCell cell, int width) {
        CTTcPr properties = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTblWidth cellWidth = properties.isSetTcW() ? properties.getTcW() : properties.addNewTcW();
        cellWidth.setType(STTblWidth.DXA);
        cellWidth.setW(BigInteger.valueOf(width));
    }

    private void mergeCellsHorizontally(XWPFTableRow row, int fromCell, int span) {
        XWPFTableCell cell = row.getCell(fromCell);
        CTTcPr properties = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTDecimalNumber gridSpan = properties.isSetGridSpan() ? properties.getGridSpan() : properties.addNewGridSpan();
        gridSpan.setVal(BigInteger.valueOf(span));
        for (int index = row.getTableCells().size() - 1; index > fromCell; index--) {
            row.removeCell(index);
        }
    }

    private void setVerticalMerge(XWPFTableCell cell, STMerge.Enum mergeType) {
        CTTcPr properties = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTVMerge verticalMerge = properties.isSetVMerge() ? properties.getVMerge() : properties.addNewVMerge();
        verticalMerge.setVal(mergeType);
    }

    private void fillCell(XWPFTableCell cell, String text, boolean bold, String fontFamily, int fontSize,
                          String color, ParagraphAlignment alignment, int spacingBefore, int spacingAfter) {
        clearCell(cell);
        appendCellParagraph(cell, text, bold, false, fontFamily, fontSize, color, alignment, spacingBefore, spacingAfter);
    }

    private void appendCellParagraph(XWPFTableCell cell, String text, boolean bold, String fontFamily, int fontSize,
                                     String color, ParagraphAlignment alignment, int spacingBefore, int spacingAfter) {
        appendCellParagraph(cell, text, bold, false, fontFamily, fontSize, color, alignment, spacingBefore, spacingAfter);
    }

    private void appendCellParagraph(XWPFTableCell cell, String text, boolean bold, boolean italic,
                                     String fontFamily, int fontSize, String color, ParagraphAlignment alignment,
                                     int spacingBefore, int spacingAfter) {
        XWPFParagraph paragraph = createCellParagraph(cell, alignment, spacingBefore, spacingAfter);
        XWPFRun run = paragraph.createRun();
        if (text != null && !text.isEmpty()) {
            run.setText(text);
        }
        run.setBold(bold);
        run.setItalic(italic);
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);
        if (color != null) {
            run.setColor(color);
        }
    }

    private XWPFParagraph createCellParagraph(XWPFTableCell cell, ParagraphAlignment alignment,
                                              int spacingBefore, int spacingAfter) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(alignment);
        paragraph.setSpacingBefore(spacingBefore);
        paragraph.setSpacingAfter(spacingAfter);
        return paragraph;
    }
}

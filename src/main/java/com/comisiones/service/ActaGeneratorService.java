package com.comisiones.service;

import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.util.AppLogger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
     * Genera un PDF de plantilla de acta completamente vacía, lista para imprimir y rellenar a mano.
     *
     * @return Contenido del PDF como array de bytes
     * @throws IOException Si hay un error al generar el PDF
     */
    public byte[] generarPlantillaVaciaPdf() throws IOException {
        AppLogger.debug("Generando plantilla de acta vacía en PDF");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth  = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float contentWidth = pageWidth - 2 * MARGIN;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = pageHeight - MARGIN;

                // ── Título principal ──────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("ACTA DE REUNIÓN:");
                cs.endText();
                y -= 22;

                // Línea en blanco para nombre del grupo/comisión
                drawUnderline(cs, MARGIN, y, contentWidth);
                y -= 20;

                // Referencia + Revisión + Página
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Referencia: _________________    Revisión: ______    Página ___ de ___");
                cs.endText();
                y -= 25;

                // ── Separador ─────────────────────────────────────────────────────────
                drawHorizontalLine(cs, MARGIN, y, contentWidth);
                y -= 15;

                // ── ASISTENTES ────────────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("ASISTENTES (Nombre y Cargo)");
                cs.endText();
                y -= 18;

                // Encabezado de columnas
                float col1 = contentWidth * 0.55f;
                float col2 = contentWidth * 0.45f;
                float rowH  = 18f;

                drawTableRow(cs, MARGIN, y, col1, col2, rowH, "Nombre", "Cargo", true);
                y -= rowH;

                // 12 filas vacías para asistentes
                for (int i = 0; i < 12; i++) {
                    drawTableRow(cs, MARGIN, y, col1, col2, rowH, "", "", false);
                    y -= rowH;
                }
                y -= 10;

                // ── Fecha y Hora / Duración ───────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Fecha y Hora: ");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN + 90, y);
                cs.showText("___________________");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN + 260, y);
                cs.showText("Duración: ");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN + 330, y);
                cs.showText("___________________");
                cs.endText();
                y -= 20;

                // ── ORDEN DEL DÍA ─────────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("ORDEN DEL DÍA:");
                cs.endText();
                y -= 16;

                for (int i = 1; i <= 6; i++) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(i + ". ");
                    cs.endText();
                    drawUnderline(cs, MARGIN + 18, y - 2, contentWidth - 18);
                    y -= 16;
                }
                y -= 8;

                // ── TIPO REUNIÓN ──────────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("TIPO REUNIÓN:");
                cs.endText();
                y -= 18;

                drawCheckbox(cs, MARGIN, y, "REVISIÓN DEL SIS. CALIDAD");
                drawCheckbox(cs, MARGIN + 180, y, "REUNIÓN INTERNA");
                y -= 18;

                drawCheckbox(cs, MARGIN, y, "OTROS (especificar): ");
                drawUnderline(cs, MARGIN + 160, y - 2, contentWidth - 160);
                y -= 22;

                // ── Excusa asistencia ─────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Excusa asistencia: ");
                cs.endText();
                drawUnderline(cs, MARGIN + 122, y - 2, contentWidth - 122);
                y -= 16;
                drawUnderline(cs, MARGIN, y - 2, contentWidth);
                y -= 22;

                // ── RESUMEN DE LA REUNIÓN ─────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("RESUMEN DE LA REUNIÓN:");
                cs.endText();
                y -= 16;

                for (int i = 0; i < 8; i++) {
                    drawUnderline(cs, MARGIN, y - 2, contentWidth);
                    y -= 16;
                }
                y -= 10;

                // ── Firma final ───────────────────────────────────────────────────────
                drawHorizontalLine(cs, MARGIN, y, contentWidth);
                y -= 15;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Nombre: ");
                cs.endText();
                drawUnderline(cs, MARGIN + 60, y - 2, contentWidth * 0.45f - 60);

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN + contentWidth * 0.5f, y);
                cs.showText("Fecha/Hora cierre: ");
                cs.endText();
                drawUnderline(cs, MARGIN + contentWidth * 0.5f + 120, y - 2, contentWidth * 0.5f - 120);
                y -= 16;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Cargo: ");
                cs.endText();
                drawUnderline(cs, MARGIN + 52, y - 2, contentWidth * 0.45f - 52);
                y -= 16;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Firma: ");
                cs.endText();
                drawUnderline(cs, MARGIN + 52, y - 2, 100);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            AppLogger.debug("Plantilla de acta vacía generada correctamente");
            return baos.toByteArray();
        }
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
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        if (bold) {
            run.setBold(true);
        }
    }
}

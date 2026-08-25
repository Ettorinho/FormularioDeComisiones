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
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        AppLogger.debug("Generando plantilla de acta vacía (AcroForm, 1 página) en PDF");

        try (PDDocument document = new PDDocument()) {
            final float PW = PDRectangle.A4.getWidth();   // 595.28
            final float PH = PDRectangle.A4.getHeight();  // 841.89
            final float M  = MARGIN;
            final float CW = PW - 2 * M;

            final float HDR_H      = 70f;
            final float HDR_LOGO_W = 130f;
            final float HDR_RIGHT_W = 120f;
            final float HDR_CENTER_W = CW - HDR_LOGO_W - HDR_RIGHT_W;
            final float HDR_TOP    = PH - M;
            final float HDR_BOT    = HDR_TOP - HDR_H;

            final float COM_H      = 28f;
            final float COM_TOP    = HDR_BOT - 6f;
            final float COM_BOT    = COM_TOP - COM_H;

            final float DATE_H     = 28f;
            final float DATE_TOP   = COM_BOT - 4f;
            final float DATE_BOT   = DATE_TOP - DATE_H;

            final float ATT_TOP    = DATE_BOT - 4f;
            final float ATT_H      = 190f;
            final float ATT_BOT    = ATT_TOP - ATT_H;
            final float ATT_COL_W  = CW * 0.5f;

            final float ORD_TOP    = ATT_BOT - 4f;
            final float ORD_H      = 88f;
            final float ORD_BOT    = ORD_TOP - ORD_H;

            final float SIG_H      = 72f;
            final float SIG_BOT    = M;
            final float SIG_TOP    = SIG_BOT + SIG_H;

            final float RES_TOP    = ORD_BOT - 4f;
            final float RES_BOT    = SIG_TOP + 4f;
            final float RES_H      = RES_TOP - RES_BOT;

            // Crear única página
            PDPage page1 = new PDPage(PDRectangle.A4);
            document.addPage(page1);

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
                        M, CW, HDR_TOP, HDR_BOT, HDR_LOGO_W, HDR_CENTER_W, HDR_RIGHT_W, HDR_H, 1, 1);
            }

            // Banda COMISIÓN DE
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, COM_BOT, CW, COM_H);
                cs.stroke();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(M + 4f, COM_TOP - 18f);
                cs.showText("COMISIÓN DE:");
                cs.endText();
            }
            addTextField(acroForm, page1, "nombreGrupo", false,
                    M + 84f, COM_BOT + 5f, CW - 88f, COM_H - 10f);

            // Fila Fecha / Hora inicio / Hora fin
            float dateColW = CW / 3f;
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, DATE_BOT, CW, DATE_H);
                cs.moveTo(M + dateColW, DATE_BOT);
                cs.lineTo(M + dateColW, DATE_TOP);
                cs.moveTo(M + 2f * dateColW, DATE_BOT);
                cs.lineTo(M + 2f * dateColW, DATE_TOP);
                cs.stroke();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(M + 4f, DATE_TOP - 18f);
                cs.showText("Fecha:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(M + dateColW + 4f, DATE_TOP - 18f);
                cs.showText("Hora inicio:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(M + 2f * dateColW + 4f, DATE_TOP - 18f);
                cs.showText("Hora fin:");
                cs.endText();
            }
            addTextField(acroForm, page1, "fecha", false,
                    M + 42f, DATE_BOT + 5f, dateColW - 46f, DATE_H - 10f);
            addTextField(acroForm, page1, "horaInicio", false,
                    M + dateColW + 68f, DATE_BOT + 5f, dateColW - 72f, DATE_H - 10f);
            addTextField(acroForm, page1, "horaFin", false,
                    M + 2f * dateColW + 54f, DATE_BOT + 5f, dateColW - 58f, DATE_H - 10f);

            // Bloque asistentes/excusas
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, ATT_BOT, CW, ATT_H);
                cs.moveTo(M + ATT_COL_W, ATT_BOT);
                cs.lineTo(M + ATT_COL_W, ATT_TOP);
                cs.moveTo(M, ATT_TOP - 18f);
                cs.lineTo(M + CW, ATT_TOP - 18f);
                cs.stroke();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9f);
                cs.newLineAtOffset(M + 3f, ATT_TOP - 13f);
                cs.showText("ASISTENTES (Nombre y Cargo)");
                cs.endText();

                float rx = M + ATT_COL_W + 3f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 8f);
                cs.newLineAtOffset(rx, ATT_TOP - 13f);
                cs.showText("EXCUSAN SU ASISTENCIA (Nombre, Cargo y Razón de la no asistencia)");
                cs.endText();
            }
            addTextField(acroForm, page1, "asistentes", true,
                    M + 2f, ATT_BOT + 2f, ATT_COL_W - 4f, ATT_H - 22f);
            addTextField(acroForm, page1, "excusaAsistencia", true,
                    M + ATT_COL_W + 2f, ATT_BOT + 2f, ATT_COL_W - 4f, ATT_H - 22f);

            // ORDEN DEL DÍA
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, ORD_BOT, CW, ORD_H);
                cs.stroke();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(M + 3f, ORD_TOP - 13f);
                cs.showText("ORDEN DEL D\u00CDA:");
                cs.endText();
            }
            addTextField(acroForm, page1, "ordenDelDia", true,
                    M + 2f, ORD_BOT + 2f, CW - 4f, ORD_H - 16f);

            // RESUMEN DE LA REUNIÓN
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, RES_BOT, CW, RES_H);
                cs.stroke();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(M + 3f, RES_TOP - 13f);
                cs.showText("RESUMEN DE LA REUNI\u00D3N:");
                cs.endText();
            }
            addScrollableTextField(acroForm, page1, "resumenReunion",
                    M + 2f, RES_BOT + 2f, CW - 4f, RES_H - 16f);

            // Bloque de firma
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.5f);
                cs.addRect(M, SIG_BOT, CW, SIG_H);
                cs.moveTo(M + CW * 0.5f, SIG_BOT);
                cs.lineTo(M + CW * 0.5f, SIG_TOP);
                cs.stroke();

                float sLb = 10f;
                float halfX = M + CW * 0.5f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(M + 3f, SIG_TOP - 14f);
                cs.showText("Nombre:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(M + 3f, SIG_TOP - 36f);
                cs.showText("Cargo:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(halfX + 3f, SIG_TOP - 14f);
                cs.showText("Fecha/Hora cierre:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(halfX + 3f, SIG_TOP - 36f);
                cs.showText("Firma:");
                cs.endText();

                cs.setLineWidth(0.5f);
                cs.addRect(halfX + 50f, SIG_BOT + 3f, 70f, 30f);
                cs.stroke();
            }

            float halfCW = CW * 0.5f;
            addTextField(acroForm, page1, "firmaNombre", false,
                    M + 52f, SIG_TOP - 28f, halfCW - 56f, 16f);
            addTextField(acroForm, page1, "firmaCargo", false,
                    M + 44f, SIG_TOP - 50f, halfCW - 48f, 16f);
            addTextField(acroForm, page1, "firmaFechaCierre", false,
                    M + halfCW + 115f, SIG_TOP - 28f, halfCW - 120f, 16f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            AppLogger.debug("Plantilla de acta vacía (AcroForm, 1 página) generada correctamente");
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
            XWPFHeaderFooterPolicy headerFooterPolicy = document.createHeaderFooterPolicy();
            XWPFHeader header = headerFooterPolicy.createHeader(STHdrFtr.DEFAULT);
            XWPFTable headerTable = header.createTable(1, 3);
            headerTable.setWidth("100%");

            // Columna izquierda: logo + subtítulo
            XWPFTableCell logoCell = headerTable.getRow(0).getCell(0);
            clearCell(logoCell);
            XWPFParagraph logoParagraph = logoCell.addParagraph();
            logoParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun logoRun = logoParagraph.createRun();
            try (InputStream logoStream = getClass().getResourceAsStream("/images/logo_salud.png")) {
                if (logoStream != null) {
                    logoRun.addPicture(logoStream, XWPFDocument.PICTURE_TYPE_PNG,
                            "logo_salud.png", Units.toEMU(95), Units.toEMU(26));
                    logoRun.addBreak();
                }
            } catch (Exception e) {
                AppLogger.debug("No se pudo insertar logo en Word: " + e.getMessage());
            }
            XWPFRun subtitleRun = logoParagraph.createRun();
            subtitleRun.setText("SECTOR DE BARBASTRO");
            subtitleRun.setFontSize(9);
            subtitleRun.setBold(true);

            // Columna central: título
            XWPFTableCell titleCell = headerTable.getRow(0).getCell(1);
            clearCell(titleCell);
            XWPFParagraph titleParagraph = titleCell.addParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("ACTA DE REUNIÓN");
            titleRun.setBold(true);
            titleRun.setFontSize(14);

            // Columna derecha: revisión y página
            XWPFTableCell rightCell = headerTable.getRow(0).getCell(2);
            clearCell(rightCell);
            XWPFParagraph revisionParagraph = rightCell.addParagraph();
            revisionParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun revisionRun = revisionParagraph.createRun();
            revisionRun.setText("Revisión A");
            revisionRun.setFontSize(10);
            revisionRun.setBold(true);

            XWPFParagraph pageParagraph = rightCell.addParagraph();
            pageParagraph.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun pageRun = pageParagraph.createRun();
            pageRun.setText("Página 1 de 1");
            pageRun.setFontSize(10);

            // Bloque COMISIÓN DE
            XWPFTable comisionTable = document.createTable(1, 1);
            comisionTable.setWidth("100%");
            setTableCellText(comisionTable.getRow(0).getCell(0), "COMISIÓN DE: ", true);

            // Fila Fecha / Hora inicio / Hora fin
            XWPFTable fechaHoraTable = document.createTable(1, 3);
            fechaHoraTable.setWidth("100%");
            setTableCellText(fechaHoraTable.getRow(0).getCell(0), "Fecha: ____________________", true);
            setTableCellText(fechaHoraTable.getRow(0).getCell(1), "Hora inicio: _______________", true);
            setTableCellText(fechaHoraTable.getRow(0).getCell(2), "Hora fin: __________________", true);

            // Tabla Asistentes / Excusan asistencia
            XWPFTable asistentesTable = document.createTable(1, 2);
            asistentesTable.setWidth("100%");
            setTableCellText(asistentesTable.getRow(0).getCell(0), "ASISTENTES (Nombre y Cargo)", true);
            setTableCellText(asistentesTable.getRow(0).getCell(1),
                    "EXCUSAN SU ASISTENCIA (Nombre, Cargo y Razón de la no asistencia)", true);
            addBlankLines(asistentesTable.getRow(0).getCell(0), 12);
            addBlankLines(asistentesTable.getRow(0).getCell(1), 12);

            // ORDEN DEL DÍA
            XWPFTable ordenTable = document.createTable(1, 1);
            ordenTable.setWidth("100%");
            setTableCellText(ordenTable.getRow(0).getCell(0), "ORDEN DEL DÍA:", true);
            addBlankLines(ordenTable.getRow(0).getCell(0), 5);

            // RESUMEN DE LA REUNIÓN
            XWPFTable resumenTable = document.createTable(1, 1);
            resumenTable.setWidth("100%");
            setTableCellText(resumenTable.getRow(0).getCell(0), "RESUMEN DE LA REUNIÓN:", true);
            addBlankLines(resumenTable.getRow(0).getCell(0), 11);

            // Firma
            XWPFTable firmaTable = document.createTable(2, 2);
            firmaTable.setWidth("100%");
            setTableCellText(firmaTable.getRow(0).getCell(0), "Nombre: ___________________________", true);
            setTableCellText(firmaTable.getRow(0).getCell(1), "Fecha/Hora cierre: _________________", true);
            setTableCellText(firmaTable.getRow(1).getCell(0), "Cargo: ____________________________", true);
            setTableCellText(firmaTable.getRow(1).getCell(1), "Firma: ____________________________", true);

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
            float cellW = logoW - 8f;
            float cellH = hdrH - 24f;
            float scale = Math.min(cellW / imgNatW, cellH / imgNatH);
            float drawW = imgNatW * scale;
            float drawH = imgNatH * scale;
            float imgX = m + 4f + (cellW - drawW) / 2f;
            float imgY = hdrBot + 16f + (cellH - drawH) / 2f;
            cs.drawImage(logoImage, imgX, imgY, drawW, drawH);
        }
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 8f);
        cs.newLineAtOffset(m + 8f, hdrBot + 6f);
        cs.showText("SECTOR DE BARBASTRO");
        cs.endText();

        // Título centrado
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14f);
        cs.newLineAtOffset(m + logoW + col2W / 2f - 63f, hdrBot + hdrH / 2f - 5f);
        cs.showText("ACTA DE REUNI\u00D3N");
        cs.endText();

        // Columna derecha: revisión y página
        float col3X = m + logoW + col2W + 5f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
        cs.newLineAtOffset(col3X, hdrTop - 24f);
        cs.showText("Revisi\u00F3n A");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10f);
        cs.newLineAtOffset(col3X, hdrTop - 44f);
        cs.showText("P\u00E1gina " + pageNumber + " de " + totalPages);
        cs.endText();
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
}

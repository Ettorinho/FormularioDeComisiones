package com.comisiones.service;

import com.comisiones.model.Acta;
import com.comisiones.model.AsistenciaActa;
import com.comisiones.util.AppLogger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        AppLogger.debug("Generando plantilla de acta vacía (AcroForm, 3 páginas) en PDF");

        try (PDDocument document = new PDDocument()) {
            final float PW = PDRectangle.A4.getWidth();   // 595.28
            final float PH = PDRectangle.A4.getHeight();  // 841.89
            final float M  = MARGIN;                       // 50
            final float CW = PW - 2 * M;                  // 495.28

            // ── Dimensiones del encabezado ───────────────────────────────────────
            final float HDR_H       = 72f;
            final float HDR_LOGO_W  = 75f;
            final float HDR_COL3_W  = 108f;
            final float HDR_COL2_W  = CW - HDR_LOGO_W - HDR_COL3_W;
            final float HDR_TOP     = PH - M;
            final float HDR_BOT     = HDR_TOP - HDR_H;

            // ── Dimensiones del bloque principal (página 1) ──────────────────────
            final float MAIN_TOP    = HDR_BOT - 4f;
            final float MAIN_H      = 210f;
            final float MAIN_BOT    = MAIN_TOP - MAIN_H;
            final float MAIN_LEFT_W = CW * 0.45f;
            final float MAIN_CTR_W  = CW * 0.35f;
            final float MAIN_RIGHT_W = CW - MAIN_LEFT_W - MAIN_CTR_W;

            // ── ORDEN DEL DÍA (página 1) ─────────────────────────────────────────
            final float ORD_TOP = MAIN_BOT - 4f;
            final float ORD_H   = 96f;
            final float ORD_BOT = ORD_TOP - ORD_H;

            // ── RESUMEN página 1 ──────────────────────────────────────────────────
            final float RES1_TOP = ORD_BOT - 4f;
            final float RES1_H   = RES1_TOP - M;

            // ── RESUMEN páginas 2 y 3 ─────────────────────────────────────────────
            final float RES_TOP_P23 = HDR_BOT - 4f;
            final float RES2_H      = RES_TOP_P23 - M;

            // ── Bloque de firma (página 3) ────────────────────────────────────────
            final float SIG_H    = 72f;
            final float RES3_H   = RES_TOP_P23 - M - SIG_H - 4f;

            // Crear 3 páginas
            PDPage page1 = new PDPage(PDRectangle.A4);
            PDPage page2 = new PDPage(PDRectangle.A4);
            PDPage page3 = new PDPage(PDRectangle.A4);
            document.addPage(page1);
            document.addPage(page2);
            document.addPage(page3);

            // Configurar AcroForm
            PDAcroForm acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
            acroForm.setNeedAppearances(true);

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 1 — Encabezado
            // ════════════════════════════════════════════════════════════════════
            try (PDPageContentStream cs = new PDPageContentStream(document, page1)) {
                drawPageHeaderStatic(cs, M, CW, HDR_TOP, HDR_BOT, HDR_LOGO_W, HDR_COL2_W, HDR_COL3_W, HDR_H, 1);
            }
            // Campos del encabezado (página 1)
            float hCol3X = M + HDR_LOGO_W + HDR_COL2_W;
            float hRowH  = HDR_H / 3f;
            addTextField(acroForm, page1, "nombreGrupo", true,
                    M + HDR_LOGO_W + 2, HDR_BOT + 2, HDR_COL2_W - 4, HDR_H - 4);
            addTextField(acroForm, page1, "referencia", false,
                    hCol3X + 2, HDR_BOT + 2 * hRowH + 1, HDR_COL3_W - 4, hRowH - 3);
            addTextField(acroForm, page1, "revision", false,
                    hCol3X + 2, HDR_BOT + hRowH + 1, HDR_COL3_W - 4, hRowH - 3);

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 1 — Bloque principal: ASISTENTES / Fecha+TipoReunión / Duración
            // ════════════════════════════════════════════════════════════════════
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {

                cs.setLineWidth(0.8f);
                // Recuadro exterior
                cs.addRect(M, MAIN_BOT, CW, MAIN_H);
                // Separador izquierda|centro
                cs.moveTo(M + MAIN_LEFT_W, MAIN_BOT);
                cs.lineTo(M + MAIN_LEFT_W, MAIN_TOP);
                // Separador centro|derecha
                cs.moveTo(M + MAIN_LEFT_W + MAIN_CTR_W, MAIN_BOT);
                cs.lineTo(M + MAIN_LEFT_W + MAIN_CTR_W, MAIN_TOP);
                cs.stroke();

                // Label columna izquierda
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(M + 3f, MAIN_TOP - 13f);
                cs.showText("ASISTENTES (Nombre y Cargo)");
                cs.endText();

                // Labels columna central
                float cx = M + MAIN_LEFT_W + 3f;
                float cy = MAIN_TOP - 13f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(cx, cy);
                cs.showText("Fecha y Hora:");
                cs.endText();
                cy -= 22f;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(cx, cy);
                cs.showText("TIPO REUNI\u00D3N:");
                cs.endText();
                cy -= 14f;

                // Checkboxes de tipo reunión
                float cbX = cx + 1f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9f);
                cs.newLineAtOffset(cbX + 14f, cy);
                cs.showText("REVISI\u00D3N DEL SIS. CALIDAD");
                cs.endText();
                cy -= 16f;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9f);
                cs.newLineAtOffset(cbX + 14f, cy);
                cs.showText("REUNI\u00D3N INTERNA");
                cs.endText();
                cy -= 16f;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9f);
                cs.newLineAtOffset(cbX + 14f, cy);
                cs.showText("OTROS (especificar):");
                cs.endText();
                cy -= 18f;

                // Label Excusa asistencia
                float excusaLabelY = MAIN_BOT + 54f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(cx, excusaLabelY);
                cs.showText("Excusa asistencia:");
                cs.endText();

                // Label columna derecha
                float rx = M + MAIN_LEFT_W + MAIN_CTR_W + 3f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10f);
                cs.newLineAtOffset(rx, MAIN_TOP - 13f);
                cs.showText("Duraci\u00F3n:");
                cs.endText();
            }

            // Campos interactivos bloque principal
            float cx1 = M + MAIN_LEFT_W + 3f;
            float cw1 = MAIN_CTR_W - 6f;

            // Asistentes (área multilínea toda la columna izquierda)
            addTextField(acroForm, page1, "asistentes", true,
                    M + 2f, MAIN_BOT + 2f, MAIN_LEFT_W - 4f, MAIN_H - 16f);

            // Fecha y Hora
            addTextField(acroForm, page1, "fechaHora", false,
                    cx1, MAIN_TOP - 36f, cw1, 16f);

            // Checkboxes tipo reunión
            float cbBaseY = MAIN_TOP - 13f - 22f - 14f;
            addCheckBox(acroForm, page1, "tipoReunionCalidad",
                    cx1 + 1f, cbBaseY - 12f, 12f);
            cbBaseY -= 16f;
            addCheckBox(acroForm, page1, "tipoReunionInterna",
                    cx1 + 1f, cbBaseY - 12f, 12f);
            cbBaseY -= 16f;
            addCheckBox(acroForm, page1, "tipoReunionOtros",
                    cx1 + 1f, cbBaseY - 12f, 12f);
            // Campo "otros especificar"
            float otrosX = cx1 + 15f + 92f;
            float otrosW = (M + MAIN_LEFT_W + MAIN_CTR_W) - otrosX - 4f;
            if (otrosW > 15f) {
                addTextField(acroForm, page1, "tipoReunionOtrosEspecificar", false,
                        otrosX, cbBaseY - 12f, otrosW, 12f);
            }

            // Excusa asistencia (área multilínea)
            float excusaFieldBot = MAIN_BOT + 2f;
            float excusaFieldTop = MAIN_BOT + 52f;
            addTextField(acroForm, page1, "excusaAsistencia", true,
                    cx1, excusaFieldBot, cw1, excusaFieldTop - excusaFieldBot);

            // Duración
            float rx1 = M + MAIN_LEFT_W + MAIN_CTR_W + 3f;
            float rw1 = MAIN_RIGHT_W - 6f;
            addTextField(acroForm, page1, "duracion", false,
                    rx1, MAIN_TOP - 36f, rw1, 16f);

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 1 — ORDEN DEL DÍA
            // ════════════════════════════════════════════════════════════════════
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

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 1 — RESUMEN DE LA REUNIÓN
            // ════════════════════════════════════════════════════════════════════
            try (PDPageContentStream cs = new PDPageContentStream(document, page1,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, M, CW, RES1_H);
                cs.stroke();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(M + 3f, M + RES1_H - 13f);
                cs.showText("RESUMEN DE LA REUNI\u00D3N:");
                cs.endText();
            }
            addTextField(acroForm, page1, "resumenReunionPagina1", true,
                    M + 2f, M + 2f, CW - 4f, RES1_H - 16f);

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 2 — Encabezado + RESUMEN (continuación)
            // ════════════════════════════════════════════════════════════════════
            try (PDPageContentStream cs = new PDPageContentStream(document, page2)) {
                drawPageHeaderStatic(cs, M, CW, HDR_TOP, HDR_BOT, HDR_LOGO_W, HDR_COL2_W, HDR_COL3_W, HDR_H, 2);
            }
            try (PDPageContentStream cs = new PDPageContentStream(document, page2,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                cs.addRect(M, M, CW, RES2_H);
                cs.stroke();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(M + 3f, M + RES2_H - 13f);
                cs.showText("RESUMEN DE LA REUNI\u00D3N (continuaci\u00F3n):");
                cs.endText();
            }
            addTextField(acroForm, page2, "resumenReunionPagina2", true,
                    M + 2f, M + 2f, CW - 4f, RES2_H - 16f);

            // ════════════════════════════════════════════════════════════════════
            // PÁGINA 3 — Encabezado + RESUMEN (continuación) + Firma
            // ════════════════════════════════════════════════════════════════════
            try (PDPageContentStream cs = new PDPageContentStream(document, page3)) {
                drawPageHeaderStatic(cs, M, CW, HDR_TOP, HDR_BOT, HDR_LOGO_W, HDR_COL2_W, HDR_COL3_W, HDR_H, 3);
            }

            float sigTop = M + SIG_H;

            try (PDPageContentStream cs = new PDPageContentStream(document, page3,
                    PDPageContentStream.AppendMode.APPEND, false)) {
                cs.setLineWidth(0.8f);
                // Recuadro RESUMEN (continuación)
                cs.addRect(M, sigTop + 4f, CW, RES3_H);
                cs.stroke();
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
                cs.newLineAtOffset(M + 3f, sigTop + 4f + RES3_H - 13f);
                cs.showText("RESUMEN DE LA REUNI\u00D3N (continuaci\u00F3n):");
                cs.endText();

                // Recuadro de firma
                cs.setLineWidth(0.5f);
                cs.addRect(M, M, CW, SIG_H);
                // Separador vertical a la mitad
                cs.moveTo(M + CW * 0.5f, M);
                cs.lineTo(M + CW * 0.5f, M + SIG_H);
                cs.stroke();

                // Labels bloque de firma
                float sLb = 10f;
                float halfX = M + CW * 0.5f;
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(M + 3f, M + SIG_H - 14f);
                cs.showText("Nombre:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(M + 3f, M + SIG_H - 36f);
                cs.showText("Cargo:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(halfX + 3f, M + SIG_H - 14f);
                cs.showText("Fecha/Hora cierre:");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, sLb);
                cs.newLineAtOffset(halfX + 3f, M + SIG_H - 36f);
                cs.showText("Firma:");
                cs.endText();
                // Recuadro visual para la firma
                cs.setLineWidth(0.5f);
                cs.addRect(halfX + 50f, M + 3f, 70f, 30f);
                cs.stroke();
            }

            addTextField(acroForm, page3, "resumenReunionPagina3", true,
                    M + 2f, sigTop + 6f, CW - 4f, RES3_H - 16f);

            float halfCW = CW * 0.5f;
            addTextField(acroForm, page3, "firmaNombre", false,
                    M + 52f, M + SIG_H - 28f, halfCW - 56f, 16f);
            addTextField(acroForm, page3, "firmaCargo", false,
                    M + 44f, M + SIG_H - 50f, halfCW - 48f, 16f);
            addTextField(acroForm, page3, "firmaFechaCierre", false,
                    M + halfCW + 115f, M + SIG_H - 28f, halfCW - 120f, 16f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            AppLogger.debug("Plantilla de acta vacía (AcroForm, 3 páginas) generada correctamente");
            return baos.toByteArray();
        }
    }

    /**
     * Dibuja el encabezado estático de 3 columnas con bordes en una página.
     * Columna 1: espacio para logo. Columna 2: título ACTA DE REUNIÓN + espacio grupo.
     * Columna 3: referencia, revisión, página. Los campos interactivos se añaden por separado.
     */
    private void drawPageHeaderStatic(PDPageContentStream cs, float m, float cw,
            float hdrTop, float hdrBot, float logoW, float col2W, float col3W, float hdrH,
            int pageNum) throws IOException {
        cs.setLineWidth(0.8f);
        // Recuadro exterior del encabezado
        cs.addRect(m, hdrBot, cw, hdrH);
        // Separador logo|col2
        cs.moveTo(m + logoW, hdrBot);
        cs.lineTo(m + logoW, hdrTop);
        // Separador col2|col3
        cs.moveTo(m + logoW + col2W, hdrBot);
        cs.lineTo(m + logoW + col2W, hdrTop);
        // Divisores horizontales columna 3 (3 filas de igual altura)
        float rowH = hdrH / 3f;
        cs.moveTo(m + logoW + col2W, hdrBot + rowH);
        cs.lineTo(m + logoW + col2W + col3W, hdrBot + rowH);
        cs.moveTo(m + logoW + col2W, hdrBot + 2f * rowH);
        cs.lineTo(m + logoW + col2W + col3W, hdrBot + 2f * rowH);
        cs.stroke();

        // Placeholder [LOGO] en columna 1
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 9f);
        cs.newLineAtOffset(m + 5f, hdrBot + hdrH / 2f - 4f);
        cs.showText("[LOGO]");
        cs.endText();

        // Título en columna 2
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 11f);
        cs.newLineAtOffset(m + logoW + 4f, hdrTop - 13f);
        cs.showText("ACTA DE REUNI\u00D3N:");
        cs.endText();

        // Etiquetas en columna 3
        float col3X = m + logoW + col2W + 3f;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 8f);
        cs.newLineAtOffset(col3X, hdrBot + 2f * rowH + rowH / 2f - 3f);
        cs.showText("Referencia:");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 8f);
        cs.newLineAtOffset(col3X, hdrBot + rowH + rowH / 2f - 3f);
        cs.showText("Revisi\u00F3n:");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 8f);
        cs.newLineAtOffset(col3X, hdrBot + rowH / 2f - 3f);
        cs.showText("P\u00E1gina " + pageNum + " de 3");
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
     * Añade un checkbox interactivo (PDCheckBox) al formulario AcroForm.
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

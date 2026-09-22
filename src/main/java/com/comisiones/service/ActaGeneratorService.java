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
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Servicio para generar documentos (PDF y Word) de actas según el diseño oficial
 * extraído del PDF real de referencia en docs/20251010 Grupo de trabajo Sistema Registro y Certifcacion.pdf.
 */
public class ActaGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DOCUMENT_CODE = "MC-2_SA(P)E";
    private static final String REVISION = "A";

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 36f;
    private static final float HEADER_HEIGHT = 78f;
    private static final float SECTION_GAP = 10f;
    private static final float BOX_PADDING = 8f;
    private static final float BODY_FONT_SIZE = 9.5f;
    private static final float SMALL_FONT_SIZE = 8f;
    private static final float TITLE_FONT_SIZE = 13f;
    private static final float LINE_HEIGHT = 12f;
    private static final float SIGNATURE_HEIGHT = 70f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);
    private static final float ATTENDEES_WIDTH = CONTENT_WIDTH * 0.56f;
    private static final float DETAILS_WIDTH = CONTENT_WIDTH - ATTENDEES_WIDTH;
    private static final float PAGE_BODY_TOP = PAGE_HEIGHT - MARGIN - HEADER_HEIGHT - SECTION_GAP;

    public byte[] generarPdf(Acta acta, List<AsistenciaActa> asistencias, int numeroActaEnComision) throws IOException {
        AppLogger.debug("Generando PDF oficial para acta ID: " + acta.getId());

        List<String> asistentes = wrapParagraphs(buildAsistentesText(asistencias, true), ATTENDEES_WIDTH - (2 * BOX_PADDING), PDType1Font.HELVETICA, BODY_FONT_SIZE);
        List<String> detalles = buildDetallesLines(acta);
        List<String> ordenDia = wrapParagraphs(defaultIfBlank(acta.getOrdenDia(), "Sin orden del día."), CONTENT_WIDTH - (2 * BOX_PADDING), PDType1Font.HELVETICA, BODY_FONT_SIZE);
        List<String> resumen = wrapParagraphs(defaultIfBlank(acta.getObservaciones(), "Sin resumen de la reunión."), CONTENT_WIDTH - (2 * BOX_PADDING), PDType1Font.HELVETICA, BODY_FONT_SIZE);

        String[] firma = resolveFirma(asistencias);
        boolean mostrarFirma = !firma[0].isEmpty() || !firma[1].isEmpty();

        PdfLayout layout = planPdfLayout(asistentes.size(), detalles.size(), ordenDia.size(), resumen.size(), mostrarFirma);

        try (PDDocument document = new PDDocument()) {
            PDImageXObject logo = loadPdfLogo(document);
            int resumenIndex = 0;

            for (int pageNumber = 1; pageNumber <= layout.totalPages; pageNumber++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    drawHeader(content, logo, acta, pageNumber, layout.totalPages);

                    if (pageNumber == 1) {
                        drawFirstPage(content, acta, asistentes, detalles, ordenDia, resumen, firma, layout);
                        resumenIndex = layout.firstPageSummaryLines;
                    } else {
                        int linesOnPage = pageNumber == layout.totalPages ? layout.lastPageSummaryLines : layout.middlePageCapacity;
                        drawSummaryPage(content, resumen, resumenIndex, linesOnPage, pageNumber == layout.totalPages ? firma : null);
                        resumenIndex += linesOnPage;
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    public byte[] generarWord(Acta acta, List<AsistenciaActa> asistencias, int numeroActaEnComision) throws IOException {
        AppLogger.debug("Generando Word oficial para acta ID: " + acta.getId());

        try (XWPFDocument document = new XWPFDocument()) {
            configureWordSection(document);

            XWPFTable headerTable = document.createTable(1, 3);
            configureTable(headerTable, 9800, 2200, 5200, 2400);
            fillHeaderTable(headerTable.getRow(0), document, acta);

            XWPFTable topTable = document.createTable(1, 2);
            configureTable(topTable, 9800, 5500, 4300);
            fillTopTable(topTable.getRow(0), acta, asistencias);

            XWPFTable ordenTable = document.createTable(1, 1);
            configureTable(ordenTable, 9800, 9800);
            fillSingleCellSection(ordenTable.getRow(0).getCell(0), "ORDEN DEL DÍA", defaultIfBlank(acta.getOrdenDia(), "Sin orden del día."));

            XWPFTable resumenTable = document.createTable(1, 1);
            configureTable(resumenTable, 9800, 9800);
            fillSingleCellSection(resumenTable.getRow(0).getCell(0), "RESUMEN DE LA REUNIÓN", defaultIfBlank(acta.getObservaciones(), "Sin resumen de la reunión."));

            String[] firma = resolveFirma(asistencias);
            if (!firma[0].isEmpty() || !firma[1].isEmpty()) {
                XWPFParagraph spacer = document.createParagraph();
                spacer.setSpacingBefore(180);

                XWPFParagraph firmaNombre = document.createParagraph();
                firmaNombre.setAlignment(ParagraphAlignment.LEFT);
                XWPFRun firmaNombreRun = firmaNombre.createRun();
                firmaNombreRun.setText(firma[0]);
                firmaNombreRun.setBold(true);
                firmaNombreRun.setFontSize(10);

                if (!firma[1].isEmpty()) {
                    XWPFParagraph firmaCargo = document.createParagraph();
                    firmaCargo.setAlignment(ParagraphAlignment.LEFT);
                    XWPFRun firmaCargoRun = firmaCargo.createRun();
                    firmaCargoRun.setText(firma[1]);
                    firmaCargoRun.setFontSize(10);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.write(baos);
            return baos.toByteArray();
        }
    }

    private PdfLayout planPdfLayout(int asistentesCount, int detallesCount, int ordenCount, int resumenCount, boolean mostrarFirma) {
        int attendeeRows = Math.max(asistentesCount, 6);
        int detailRows = Math.max(detallesCount, 10);
        float topBoxHeight = Math.max(170f, BOX_PADDING * 2 + Math.max(attendeeRows, detailRows) * LINE_HEIGHT + 30f);
        float ordenHeight = Math.max(72f, BOX_PADDING * 2 + ordenCount * LINE_HEIGHT + 20f);
        float summaryTop = PAGE_BODY_TOP - topBoxHeight - SECTION_GAP - ordenHeight - SECTION_GAP;
        float summaryHeight = summaryTop - MARGIN;
        int firstPageCapacityWithSignature = Math.max(1, linesCapacity(summaryHeight - SIGNATURE_HEIGHT, true));
        if (resumenCount <= firstPageCapacityWithSignature) {
            return new PdfLayout(1, topBoxHeight, ordenHeight, firstPageCapacityWithSignature,
                    linesCapacity(summaryHeight, true), firstPageCapacityWithSignature);
        }

        int firstPageCapacity = Math.max(1, linesCapacity(summaryHeight, true));
        int remaining = Math.max(0, resumenCount - firstPageCapacity);
        int middleCapacity = Math.max(1, linesCapacity(PAGE_BODY_TOP - MARGIN, true));
        int lastCapacity = Math.max(1, linesCapacity(PAGE_BODY_TOP - MARGIN - (mostrarFirma ? SIGNATURE_HEIGHT : 0f), true));

        int additionalPages = 1;
        while (remaining > lastCapacity + Math.max(0, additionalPages - 1) * middleCapacity) {
            additionalPages++;
        }

        return new PdfLayout(1 + additionalPages, topBoxHeight, ordenHeight, firstPageCapacity, middleCapacity, lastCapacity);
    }

    private int linesCapacity(float height, boolean hasTitle) {
        float usable = height - BOX_PADDING * 2 - (hasTitle ? 20f : 0f);
        return Math.max(1, (int) Math.floor(usable / LINE_HEIGHT));
    }

    private void drawFirstPage(PDPageContentStream content, Acta acta, List<String> asistentes, List<String> detalles,
                               List<String> ordenDia, List<String> resumen, String[] firma, PdfLayout layout) throws IOException {
        float topY = PAGE_BODY_TOP;
        float attendeesHeight = layout.topBoxHeight;
        float attendeesBottom = topY - attendeesHeight;
        float detailX = MARGIN + ATTENDEES_WIDTH;

        drawBox(content, MARGIN, attendeesBottom, ATTENDEES_WIDTH, attendeesHeight);
        drawSectionTitle(content, "ASISTENTES", MARGIN, topY - 16f);
        drawItalicCentered(content, "(Nombre y cargo)", MARGIN, topY - 29f, ATTENDEES_WIDTH);
        drawTextLines(content, asistentes, MARGIN + BOX_PADDING, topY - 46f, PDType1Font.HELVETICA, BODY_FONT_SIZE, LINE_HEIGHT,
                attendeesHeight - 52f);

        drawBox(content, detailX, attendeesBottom, DETAILS_WIDTH, attendeesHeight);
        drawDetailLines(content, detalles, detailX + BOX_PADDING, topY - 16f, DETAILS_WIDTH - (2 * BOX_PADDING));

        float ordenTop = attendeesBottom - SECTION_GAP;
        float ordenBottom = ordenTop - layout.ordenHeight;
        drawBox(content, MARGIN, ordenBottom, CONTENT_WIDTH, layout.ordenHeight);
        drawSectionTitle(content, "ORDEN DEL DÍA", MARGIN + BOX_PADDING, ordenTop - 16f);
        drawTextLines(content, ordenDia, MARGIN + BOX_PADDING, ordenTop - 34f, PDType1Font.HELVETICA, BODY_FONT_SIZE, LINE_HEIGHT,
                layout.ordenHeight - 38f);

        float resumenTop = ordenBottom - SECTION_GAP;
        float resumenBottom = MARGIN;
        drawBox(content, MARGIN, resumenBottom, CONTENT_WIDTH, resumenTop - resumenBottom);
        drawSectionTitle(content, "RESUMEN DE LA REUNIÓN", MARGIN + BOX_PADDING, resumenTop - 16f);
        drawTextLines(content, resumen.subList(0, Math.min(layout.firstPageSummaryLines, resumen.size())),
                MARGIN + BOX_PADDING, resumenTop - 34f, PDType1Font.HELVETICA, BODY_FONT_SIZE, LINE_HEIGHT,
                (resumenTop - resumenBottom) - 38f - (layout.totalPages == 1 && (!firma[0].isEmpty() || !firma[1].isEmpty()) ? SIGNATURE_HEIGHT : 0f));

        if (layout.totalPages == 1 && (!firma[0].isEmpty() || !firma[1].isEmpty())) {
            drawFirma(content, firma, MARGIN + BOX_PADDING, resumenBottom + 14f);
        }
    }

    private void drawSummaryPage(PDPageContentStream content, List<String> resumen, int startIndex, int linesOnPage, String[] firma) throws IOException {
        float boxBottom = MARGIN;
        float boxHeight = PAGE_BODY_TOP - boxBottom;
        drawBox(content, MARGIN, boxBottom, CONTENT_WIDTH, boxHeight);
        drawSectionTitle(content, "RESUMEN DE LA REUNIÓN (continuación)", MARGIN + BOX_PADDING, PAGE_BODY_TOP - 16f);

        boolean withSignature = firma != null && (!firma[0].isEmpty() || !firma[1].isEmpty());
        float reserved = withSignature ? SIGNATURE_HEIGHT : 0f;
        int endIndex = Math.min(resumen.size(), startIndex + linesOnPage);
        drawTextLines(content, resumen.subList(startIndex, endIndex), MARGIN + BOX_PADDING, PAGE_BODY_TOP - 34f,
                PDType1Font.HELVETICA, BODY_FONT_SIZE, LINE_HEIGHT, boxHeight - 38f - reserved);

        if (withSignature) {
            drawFirma(content, firma, MARGIN + BOX_PADDING, boxBottom + 14f);
        }
    }

    private void drawHeader(PDPageContentStream content, PDImageXObject logo, Acta acta, int pageNumber, int totalPages) throws IOException {
        float leftWidth = 115f;
        float rightWidth = 95f;
        float centerWidth = CONTENT_WIDTH - leftWidth - rightWidth;
        float top = PAGE_HEIGHT - MARGIN;
        float bottom = top - HEADER_HEIGHT;

        drawBox(content, MARGIN, bottom, CONTENT_WIDTH, HEADER_HEIGHT);
        drawVerticalLine(content, MARGIN + leftWidth, bottom, top);
        drawVerticalLine(content, MARGIN + leftWidth + centerWidth, bottom, top);

        if (logo != null) {
            float availableWidth = leftWidth - 16f;
            float availableHeight = HEADER_HEIGHT - 28f;
            float scale = Math.min(availableWidth / logo.getWidth(), availableHeight / logo.getHeight());
            float drawWidth = logo.getWidth() * scale;
            float drawHeight = logo.getHeight() * scale;
            float x = MARGIN + 8f + (availableWidth - drawWidth) / 2f;
            float y = bottom + 18f + (availableHeight - drawHeight) / 2f;
            content.drawImage(logo, x, y, drawWidth, drawHeight);
        }
        drawCentered(content, "SECTOR DE BARBASTRO", MARGIN, bottom + 7f, leftWidth, PDType1Font.HELVETICA_BOLD, SMALL_FONT_SIZE);

        float centerX = MARGIN + leftWidth;
        drawCentered(content, "ACTA DE REUNIÓN:", centerX, top - 18f, centerWidth, PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
        drawCentered(content, DOCUMENT_CODE, centerX, top - 33f, centerWidth, PDType1Font.HELVETICA_BOLD, BODY_FONT_SIZE);
        String nombreComision = acta.getComision() != null ? acta.getComision().getNombre() : "Comisión sin nombre";
        List<String> headerLines = wrapLine(nombreComision, centerWidth - 18f, PDType1Font.HELVETICA, BODY_FONT_SIZE);
        float headerY = top - 49f;
        for (String line : headerLines) {
            drawCentered(content, line, centerX, headerY, centerWidth, PDType1Font.HELVETICA, BODY_FONT_SIZE);
            headerY -= 11f;
        }

        float rightX = MARGIN + leftWidth + centerWidth;
        drawCentered(content, "Revisión: " + REVISION, rightX, top - 25f, rightWidth, PDType1Font.HELVETICA_BOLD, BODY_FONT_SIZE);
        drawCentered(content, "Página " + pageNumber + " de " + totalPages, rightX, top - 43f, rightWidth, PDType1Font.HELVETICA_BOLD, BODY_FONT_SIZE);
    }

    private List<String> buildDetallesLines(Acta acta) throws IOException {
        List<String> lines = new ArrayList<>();
        String fechaHora = formatDate(acta.getFechaReunion());
        if (!isBlank(acta.getHoraInicio())) {
            fechaHora += "       " + acta.getHoraInicio() + "h";
        }
        lines.add("Fecha y Hora: " + fechaHora);
        lines.add("Duración: " + defaultIfBlank(acta.getDuracion(), "—"));
        lines.add("");
        lines.add("TIPO REUNIÓN:");
        lines.add(marcaTipo("CALIDAD", acta) + " Revisión del Sist. Calidad");
        lines.add(marcaTipo("INTERNA", acta) + " Reunión Interna");
        String otros = marcaTipo("OTROS", acta) + " Otros";
        if (!isBlank(acta.getTipoReunionOtrosDetalle())) {
            otros += " (" + acta.getTipoReunionOtrosDetalle() + ")";
        }
        lines.add(otros);
        lines.add("");
        lines.add("Excusa asistencia:");
        lines.addAll(wrapLine(defaultIfBlank(acta.getExcusaAsistencia(), "—"), DETAILS_WIDTH - (2 * BOX_PADDING), PDType1Font.HELVETICA, BODY_FONT_SIZE));
        return lines;
    }

    private String marcaTipo(String tipo, Acta acta) {
        return tipo.equals(acta.getTipoReunion()) ? "[X]" : "[ ]";
    }

    private String buildAsistentesText(List<AsistenciaActa> asistencias, boolean asistentes) {
        if (asistencias == null || asistencias.isEmpty()) {
            return asistentes ? "Sin asistentes registrados." : "Sin ausencias registradas.";
        }
        List<String> lines = new ArrayList<>();
        for (AsistenciaActa asistencia : asistencias) {
            if (asistencia.isAsistio() != asistentes) {
                continue;
            }
            StringBuilder line = new StringBuilder(asistencia.getMiembro().getNombreApellidos());
            if (!isBlank(asistencia.getCargoMiembro())) {
                line.append(" - ").append(formatCargo(asistencia.getCargoMiembro()));
            }
            if (!asistentes && !isBlank(asistencia.getJustificacion())) {
                line.append(" (").append(asistencia.getJustificacion()).append(")");
            }
            lines.add(line.toString());
        }
        if (lines.isEmpty()) {
            return asistentes ? "Sin asistentes registrados." : "Sin ausencias registradas.";
        }
        return String.join("\n", lines);
    }

    private String[] resolveFirma(List<AsistenciaActa> asistencias) {
        if (asistencias != null) {
            for (String preferredCargo : List.of("FIRMANTE", "PRESIDENTE", "RESPONSABLE", "SECRETARIO")) {
                for (AsistenciaActa asistencia : asistencias) {
                    if (asistencia.isAsistio() && preferredCargo.equals(asistencia.getCargoMiembro())) {
                        return new String[]{asistencia.getMiembro().getNombreApellidos(), formatCargo(asistencia.getCargoMiembro())};
                    }
                }
            }
            for (AsistenciaActa asistencia : asistencias) {
                if (asistencia.isAsistio()) {
                    return new String[]{asistencia.getMiembro().getNombreApellidos(), formatCargo(asistencia.getCargoMiembro())};
                }
            }
        }
        return new String[]{"", ""};
    }

    private String formatCargo(String cargo) {
        if (isBlank(cargo)) {
            return "";
        }
        return cargo.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private void drawDetailLines(PDPageContentStream content, List<String> lines, float x, float topY, float width) throws IOException {
        float currentY = topY;
        for (String line : lines) {
            List<String> wrapped = line.isEmpty()
                    ? Collections.singletonList("")
                    : wrapLine(line, width, PDType1Font.HELVETICA, BODY_FONT_SIZE);
            for (String wrappedLine : wrapped) {
                drawLeft(content, wrappedLine, x, currentY, PDType1Font.HELVETICA, BODY_FONT_SIZE);
                currentY -= LINE_HEIGHT;
            }
        }
    }

    private void drawTextLines(PDPageContentStream content, List<String> lines, float x, float topY,
                               PDType1Font font, float fontSize, float lineHeight, float maxHeight) throws IOException {
        float currentY = topY;
        float bottomLimit = topY - maxHeight;
        for (String line : lines) {
            if (currentY < bottomLimit) {
                break;
            }
            drawLeft(content, line, x, currentY, font, fontSize);
            currentY -= lineHeight;
        }
    }

    private void drawFirma(PDPageContentStream content, String[] firma, float x, float y) throws IOException {
        if (!firma[0].isEmpty()) {
            drawLeft(content, firma[0], x, y + 18f, PDType1Font.HELVETICA_BOLD, BODY_FONT_SIZE);
        }
        if (!firma[1].isEmpty()) {
            drawLeft(content, firma[1], x, y + 4f, PDType1Font.HELVETICA, BODY_FONT_SIZE);
        }
    }

    private void fillHeaderTable(XWPFTableRow row, XWPFDocument document, Acta acta) throws IOException {
        XWPFTableCell left = row.getCell(0);
        clearCell(left);
        XWPFParagraph logoParagraph = left.addParagraph();
        logoParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun logoRun = logoParagraph.createRun();
        try (InputStream stream = getClass().getResourceAsStream("/images/logo_salud.png")) {
            if (stream != null) {
                logoRun.addPicture(stream, XWPFDocument.PICTURE_TYPE_PNG, "logo_salud.png", Units.toEMU(92), Units.toEMU(46));
            }
        } catch (IOException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
            AppLogger.debug("No se pudo insertar logo en Word: " + e.getMessage());
        }
        XWPFRun leftRun = left.addParagraph().createRun();
        leftRun.setBold(true);
        leftRun.setFontSize(8);
        leftRun.setText("SECTOR DE BARBASTRO");

        XWPFTableCell center = row.getCell(1);
        clearCell(center);
        appendCentered(center, "ACTA DE REUNIÓN:", true, 12);
        appendCentered(center, DOCUMENT_CODE, true, 10);
        appendCentered(center, acta.getComision() != null ? acta.getComision().getNombre() : "Comisión sin nombre", false, 10);

        XWPFTableCell right = row.getCell(2);
        clearCell(right);
        appendCentered(right, "Revisión: " + REVISION, true, 9);
        XWPFParagraph pageParagraph = right.addParagraph();
        pageParagraph.setAlignment(ParagraphAlignment.CENTER);
        addWordPageField(pageParagraph);
    }

    private void fillTopTable(XWPFTableRow row, Acta acta, List<AsistenciaActa> asistencias) {
        XWPFTableCell left = row.getCell(0);
        clearCell(left);
        appendCentered(left, "ASISTENTES", true, 10);
        appendCentered(left, "(Nombre y cargo)", false, 9);
        appendMultiline(left, buildAsistentesText(asistencias, true), false, 9);

        XWPFTableCell right = row.getCell(1);
        clearCell(right);
        appendLabelValue(right, "Fecha y Hora", formatDate(acta.getFechaReunion()) + (isBlank(acta.getHoraInicio()) ? "" : "       " + acta.getHoraInicio() + "h"));
        appendLabelValue(right, "Duración", defaultIfBlank(acta.getDuracion(), "—"));
        appendLabelValue(right, "TIPO REUNIÓN", "");
        appendMultiline(right,
                marcaTipo("CALIDAD", acta) + " Revisión del Sist. Calidad\n"
                        + marcaTipo("INTERNA", acta) + " Reunión Interna\n"
                        + marcaTipo("OTROS", acta) + " Otros" + (isBlank(acta.getTipoReunionOtrosDetalle()) ? "" : " (" + acta.getTipoReunionOtrosDetalle() + ")"),
                false, 9);
        appendLabelValue(right, "Excusa asistencia", defaultIfBlank(acta.getExcusaAsistencia(), "—"));
    }

    private void fillSingleCellSection(XWPFTableCell cell, String title, String body) {
        clearCell(cell);
        XWPFParagraph titleParagraph = cell.addParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(10);
        titleRun.setText(title);
        appendMultiline(cell, body, false, 9);
    }

    private void addWordPageField(XWPFParagraph paragraph) {
        XWPFRun prefix = paragraph.createRun();
        prefix.setBold(true);
        prefix.setFontSize(9);
        prefix.setText("Página ");

        addFieldRun(paragraph, "PAGE");

        XWPFRun middle = paragraph.createRun();
        middle.setBold(true);
        middle.setFontSize(9);
        middle.setText(" de ");

        addFieldRun(paragraph, "NUMPAGES");
    }

    private void addFieldRun(XWPFParagraph paragraph, String instruction) {
        CTP ctp = paragraph.getCTP();
        CTR begin = ctp.addNewR();
        begin.addNewFldChar().setFldCharType(STFldCharType.BEGIN);

        CTR instr = ctp.addNewR();
        CTText text = instr.addNewInstrText();
        text.setStringValue(instruction);

        CTR separate = ctp.addNewR();
        separate.addNewFldChar().setFldCharType(STFldCharType.SEPARATE);

        CTR display = ctp.addNewR();
        display.addNewT().setStringValue("1");

        CTR end = ctp.addNewR();
        end.addNewFldChar().setFldCharType(STFldCharType.END);
    }

    private void configureWordSection(XWPFDocument document) {
        CTSectPr section = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();

        CTPageSz pageSize = section.isSetPgSz() ? section.getPgSz() : section.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906));
        pageSize.setH(BigInteger.valueOf(16838));
        pageSize.setOrient(STPageOrientation.PORTRAIT);

        CTPageMar margins = section.isSetPgMar() ? section.getPgMar() : section.addNewPgMar();
        margins.setTop(BigInteger.valueOf(720));
        margins.setBottom(BigInteger.valueOf(720));
        margins.setLeft(BigInteger.valueOf(720));
        margins.setRight(BigInteger.valueOf(720));
        margins.setHeader(BigInteger.valueOf(360));
        margins.setFooter(BigInteger.valueOf(360));
    }

    private void configureTable(XWPFTable table, int totalWidth, int... columnWidths) {
        table.setTableAlignment(TableRowAlign.CENTER);
        table.setCellMargins(70, 70, 70, 70);

        CTTblPr tableProperties = table.getCTTbl().getTblPr() != null ? table.getCTTbl().getTblPr() : table.getCTTbl().addNewTblPr();
        CTTblWidth width = tableProperties.isSetTblW() ? tableProperties.getTblW() : tableProperties.addNewTblW();
        width.setType(STTblWidth.DXA);
        width.setW(BigInteger.valueOf(totalWidth));

        CTTblLayoutType layout = tableProperties.isSetTblLayout() ? tableProperties.getTblLayout() : tableProperties.addNewTblLayout();
        layout.setType(STTblLayoutType.FIXED);

        CTTblBorders borders = tableProperties.isSetTblBorders() ? tableProperties.getTblBorders() : tableProperties.addNewTblBorders();
        setBorder(borders.isSetTop() ? borders.getTop() : borders.addNewTop());
        setBorder(borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom());
        setBorder(borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft());
        setBorder(borders.isSetRight() ? borders.getRight() : borders.addNewRight());
        setBorder(borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH());
        setBorder(borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV());

        CTTblGrid grid = table.getCTTbl().getTblGrid() != null ? table.getCTTbl().getTblGrid() : table.getCTTbl().addNewTblGrid();
        while (grid.sizeOfGridColArray() > 0) {
            grid.removeGridCol(0);
        }
        for (int columnWidth : columnWidths) {
            CTTblGridCol column = grid.addNewGridCol();
            column.setW(BigInteger.valueOf(columnWidth));
        }

        for (XWPFTableRow row : table.getRows()) {
            for (int i = 0; i < row.getTableCells().size() && i < columnWidths.length; i++) {
                CTTcPr properties = row.getCell(i).getCTTc().isSetTcPr() ? row.getCell(i).getCTTc().getTcPr() : row.getCell(i).getCTTc().addNewTcPr();
                CTTblWidth cellWidth = properties.isSetTcW() ? properties.getTcW() : properties.addNewTcW();
                cellWidth.setType(STTblWidth.DXA);
                cellWidth.setW(BigInteger.valueOf(columnWidths[i]));
            }
        }
    }

    private void setBorder(CTBorder border) {
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(6));
        border.setColor("000000");
    }

    private void appendCentered(XWPFTableCell cell, String text, boolean bold, int fontSize) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(bold);
        run.setFontSize(fontSize);
    }

    private void appendLabelValue(XWPFTableCell cell, String label, String value) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun labelRun = paragraph.createRun();
        labelRun.setBold(true);
        labelRun.setFontSize(9);
        labelRun.setText(label + ": ");
        XWPFRun valueRun = paragraph.createRun();
        valueRun.setFontSize(9);
        valueRun.setText(value);
    }

    private void appendMultiline(XWPFTableCell cell, String text, boolean bold, int fontSize) {
        for (String line : text.split("\\R", -1)) {
            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun run = paragraph.createRun();
            run.setFontSize(fontSize);
            run.setBold(bold);
            run.setText(line);
        }
    }

    private void clearCell(XWPFTableCell cell) {
        for (int i = cell.getParagraphs().size() - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
    }

    private PDImageXObject loadPdfLogo(PDDocument document) {
        try (InputStream stream = getClass().getResourceAsStream("/images/logo_salud.png")) {
            if (stream == null) {
                return null;
            }
            return PDImageXObject.createFromByteArray(document, stream.readAllBytes(), "logo_salud");
        } catch (IOException e) {
            AppLogger.debug("No se pudo cargar el logo para el PDF: " + e.getMessage());
            return null;
        }
    }

    private List<String> wrapParagraphs(String text, float width, PDType1Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.replace("\r", "").split("\n", -1);
        for (int i = 0; i < paragraphs.length; i++) {
            lines.addAll(wrapLine(paragraphs[i], width, font, fontSize));
            if (i < paragraphs.length - 1) {
                lines.add("");
            }
        }
        return lines;
    }

    private List<String> wrapLine(String text, float width, PDType1Font font, float fontSize) throws IOException {
        if (isBlank(text)) {
            return Collections.singletonList("");
        }
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (font.getStringWidth(candidate) / 1000f * fontSize <= width) {
                current.setLength(0);
                current.append(candidate);
            } else {
                if (current.length() > 0) {
                    lines.add(current.toString());
                }
                current.setLength(0);
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines.isEmpty() ? Collections.singletonList("") : lines;
    }

    private void drawBox(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.setLineWidth(0.8f);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawVerticalLine(PDPageContentStream content, float x, float y1, float y2) throws IOException {
        content.moveTo(x, y1);
        content.lineTo(x, y2);
        content.stroke();
    }

    private void drawSectionTitle(PDPageContentStream content, String text, float x, float y) throws IOException {
        drawLeft(content, text, x, y, PDType1Font.HELVETICA_BOLD, BODY_FONT_SIZE);
    }

    private void drawItalicCentered(PDPageContentStream content, String text, float x, float y, float width) throws IOException {
        drawCentered(content, text, x, y, width, PDType1Font.HELVETICA_OBLIQUE, SMALL_FONT_SIZE);
    }

    private void drawLeft(PDPageContentStream content, String text, float x, float y, PDType1Font font, float fontSize) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    private void drawCentered(PDPageContentStream content, String text, float x, float y, float width, PDType1Font font, float fontSize) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * fontSize;
        drawLeft(content, text, x + Math.max(0f, (width - textWidth) / 2f), y, font, fontSize);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class PdfLayout {
        private final int totalPages;
        private final float topBoxHeight;
        private final float ordenHeight;
        private final int firstPageSummaryLines;
        private final int middlePageCapacity;
        private final int lastPageSummaryLines;

        private PdfLayout(int totalPages, float topBoxHeight, float ordenHeight, int firstPageSummaryLines,
                          int middlePageCapacity, int lastPageSummaryLines) {
            this.totalPages = totalPages;
            this.topBoxHeight = topBoxHeight;
            this.ordenHeight = ordenHeight;
            this.firstPageSummaryLines = firstPageSummaryLines;
            this.middlePageCapacity = middlePageCapacity;
            this.lastPageSummaryLines = lastPageSummaryLines;
        }
    }
}

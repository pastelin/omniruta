package com.HealthAdvanced.healthAdvanced.HEADPrescription.service;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignaturePointDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureStrokeDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADSignatureVectorDto;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescriptionMedication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import java.util.Optional;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class HEADPrescriptionPdfService {

    private final ObjectMapper om;

    public HEADPrescriptionPdfService(ObjectMapper om) {
        this.om = om;
    }

    // ============================
    // PUBLIC API
    // ============================
    public byte[] buildPdf(HEADPrescription rx) {
        try (PDDocument doc = new PDDocument()) {

            // Fonts
            final PDFont font = PDType1Font.HELVETICA;
            final PDFont fontBold = PDType1Font.HELVETICA_BOLD;

            // Layout
            final float margin = 46f;
            final float headerH = 92f;
            final float footerH = 34f;

            // Start ctx (creates first page + draws header)
            PdfCtx ctx = PdfCtx.start(doc, margin, headerH, footerH, font, fontBold, rx);

            // Title + meta strip
            ctx = drawTitleStrip(ctx, rx);

            // Doctor + Patient cards (two cards)
            ctx = drawDoctorPatientCards(ctx, rx);

            // Diagnosis
            ctx = drawSectionBox(
                    ctx,
                    "Diagnóstico",
                    nullSafe(rx.getDiagnosis()),
                    0.0f
            );

            // Medications table (PRO, multi-page, repeated header)
            ctx = drawMedsTable(ctx, rx.getMedications());

            // Additional instructions
            ctx = drawSectionBox(
                    ctx,
                    "Instrucciones adicionales",
                    nullSafe(rx.getAdditionalInstructions()),
                    0.0f
            );

            // Signature block
            ctx = drawSignatureBlock(ctx, rx);

            // Close last stream
            ctx.closeStream();

            // Stamp page numbers (x / total)
            stampPageNumbers(doc, font, 9.5f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF_GENERATION_FAILED: " + e.getMessage(), e);
        }
    }

    // ============================
    // HEADER / FOOTER / PAGE NUMBERS
    // ============================
    private void stampPageNumbers(PDDocument doc, PDFont font, float fontSize) throws IOException {
        int total = doc.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            PDPage page = doc.getPage(i);
            float w = page.getMediaBox().getWidth();
            float x = w - 70;
            float y = 18;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page, AppendMode.APPEND, true)) {
                cs.setNonStrokingColor(new Color(90, 98, 110));
                drawText(cs, font, fontSize, x, y, (i + 1) + " / " + total);
                cs.setNonStrokingColor(Color.BLACK);
            }
        }
    }

    // ============================
    // SECTIONS
    // ============================
    private PdfCtx drawTitleStrip(PdfCtx ctx, HEADPrescription rx) throws IOException {
        ctx.ensureSpace(42);

        float x = ctx.m;
        float yTop = ctx.y;

        // Accent strip
        setFill(ctx.cs, Colors.BLUE_600);
        ctx.cs.addRect(x, yTop - 28, ctx.pageW(), 28);
        ctx.cs.fill();

        setNonStroking(ctx.cs, Color.WHITE);
        drawText(ctx.cs, ctx.fontBold, 16f, x + 14, yTop - 20, "Receta Médica");

        // Right meta
        String code = "Folio: " + nullSafe(rx.getPrescriptionCode());
        String date = "Fecha: " + nullSafe(rx.getDateText());
        float metaX = x + ctx.pageW() - 14;
        drawTextRight(ctx.cs, ctx.font, 10.5f, metaX, yTop - 14, code);
        drawTextRight(ctx.cs, ctx.font, 10.5f, metaX, yTop - 26, date);

        setNonStroking(ctx.cs, Color.BLACK);

        ctx.y = yTop - 38;
        return ctx;
    }

    private PdfCtx drawDoctorPatientCards(PdfCtx ctx, HEADPrescription rx) throws IOException {
        // Cards layout
        final float gap = 12f;
        final float cardH = 92f;
        final float x = ctx.m;
        final float w = (ctx.pageW() - gap) / 2f;

        ctx.ensureSpace(cardH + 18);

        float yTop = ctx.y;

        // Doctor card
        drawCard(
                ctx,
                x, yTop,
                w, cardH,
                "Doctor",
                List.of(
                        row("Nombre", rx.getDoctorName()),
                        row("Cédula", rx.getDoctorLicenseNo()),
                        row("Especialidad", rx.getDoctorSpecialty()),
                        row("Clínica", rx.getDoctorClinicName())
                )
        );

        // Patient card
        drawCard(
                ctx,
                x + w + gap, yTop,
                w, cardH,
                "Paciente",
                List.of(
                        row("Nombre", rx.getPatientName()),
                        row("Edad", rx.getPatientAge() == null ? null : String.valueOf(rx.getPatientAge())),
                        row("Género", rx.getPatientGender()),
                        row("Dirección", rx.getPatientAddress())
                )
        );

        ctx.y = yTop - cardH - 16;
        return ctx;
    }

    private PdfCtx drawSectionBox(PdfCtx ctx, String title, String text, float minHeight) throws IOException {
        text = (text == null) ? "" : text.trim();
        if (text.isEmpty() || "—".equals(text)) {
            // still draw a small section for consistent layout
            text = "—";
        }

        final float x = ctx.m;
        final float w = ctx.pageW();
        final float pad = 12f;
        final float titleH = 20f;

        // Wrap
        float fontSize = 10.8f;
        float leading = 13.2f;
        List<String> lines = wrapText(text, ctx.font, fontSize, w - (pad * 2));

        float contentH = Math.max(minHeight, (lines.size() * leading) + 8);
        float boxH = titleH + pad + contentH + pad;

        ctx.ensureSpace(boxH + 10);

        float yTop = ctx.y;

        // Box bg
        setFill(ctx.cs, Colors.GRAY_050);
        ctx.cs.addRect(x, yTop - boxH, w, boxH);
        ctx.cs.fill();

        // Border
        setStroking(ctx.cs, Colors.GRAY_200);
        ctx.cs.addRect(x, yTop - boxH, w, boxH);
        ctx.cs.stroke();
        setStroking(ctx.cs, Color.BLACK);

        // Title strip
        setFill(ctx.cs, Colors.BLUE_050);
        ctx.cs.addRect(x, yTop - titleH, w, titleH);
        ctx.cs.fill();

        setNonStroking(ctx.cs, Colors.BLUE_700);
        drawText(ctx.cs, ctx.fontBold, 11.5f, x + pad, yTop - 14, title);

        // Content
        setNonStroking(ctx.cs, Color.BLACK);
        float tx = x + pad;
        float ty = yTop - titleH - pad - fontSize;
        drawMultiline(ctx.cs, ctx.font, fontSize, leading, tx, ty, lines);

        ctx.y = yTop - boxH - 12;
        return ctx;
    }

    // ============================
    // MEDS TABLE (PRO)
    // ============================
    private PdfCtx drawMedsTable(PdfCtx ctx, List<HEADPrescriptionMedication> meds) throws IOException {
        meds = meds == null ? List.of() : meds;

        // Sort by lineNo if present, else stable
        List<HEADPrescriptionMedication> sorted = new ArrayList<>(meds);
        sorted.sort(Comparator
                .comparing((HEADPrescriptionMedication m) -> m.getLineNo() == null ? Integer.MAX_VALUE : m.getLineNo())
                .thenComparing(m -> m.getId() == null ? Long.MAX_VALUE : m.getId())
        );

        // Table layout
        final float tableX = ctx.m;
        final float tableW = ctx.pageW();
        final float padX = 8f;
        final float rowPadY = 8f;

        final boolean many = sorted.size() > 30;
        final float fontSize = many ? 9.6f : 10.6f;
        final float leading = many ? 13.2f : 14.4f;

        // Column widths (proportional feel)
        final float wNo = 26f;
        final float wName = 160f;
        final float wDose = 72f;
        final float wFreq = 96f;
        final float wDur = 64f;
        final float wInstr = tableW - (wNo + wName + wDose + wFreq + wDur);

        // Section title
        ctx.ensureSpace(32);
        setNonStroking(ctx.cs, Colors.BLUE_700);
        drawText(ctx.cs, ctx.fontBold, 12f, tableX, ctx.y - 14, "Medicamentos");
        setNonStroking(ctx.cs, Color.BLACK);
        ctx.y -= 22;

        // Header
        ctx = drawMedsHeader(ctx, tableX, ctx.y, tableW, fontSize, wNo, wName, wDose, wFreq, wDur, wInstr);
        ctx.y -= 10;

        if (sorted.isEmpty()) {
            ctx.ensureSpace(34);
            setNonStroking(ctx.cs, new Color(90, 98, 110));
            drawText(ctx.cs, ctx.font, 10.6f, tableX + 10, ctx.y - 18, "— Sin medicamentos —");
            setNonStroking(ctx.cs, Color.BLACK);
            ctx.y -= 26;
            return ctx;
        }

        boolean zebra = false;

        for (int i = 0; i < sorted.size(); i++) {
            HEADPrescriptionMedication m = sorted.get(i);

            String no = String.valueOf(
                    m.getLineNo() != null ? m.getLineNo() : (i + 1)
            );

            String name = nullSafe(m.getName());
            String dose = nullSafe(m.getDosage());
            String freq = nullSafe(m.getFrequency());
            String dur = nullSafe(m.getDuration());
            String instr = nullSafe(m.getInstructions());

            // Wrap heavy columns
            List<String> nameLines = wrapText(name, ctx.font, fontSize, wName - (padX * 2));
            List<String> instrLines = wrapText(instr, ctx.font, fontSize, wInstr - (padX * 2));

            // Limit instructions to 3 lines to keep pro look
            instrLines = ellipsizeLines(instrLines, 3);

            int maxLines = Math.max(1, Math.max(nameLines.size(), instrLines.size()));
            float rowH = (maxLines * leading) + (rowPadY * 2);

            // Ensure space; if new page, redraw header
            PdfCtx.Space space = ctx.ensureSpaceWithNewPage(rowH + 24);
            ctx = space.ctx;

            if (space.newPage) {
                // re-title small
                setNonStroking(ctx.cs, Colors.BLUE_700);
                drawText(ctx.cs, ctx.fontBold, 12f, tableX, ctx.y - 14, "Medicamentos (continuación)");
                setNonStroking(ctx.cs, Color.BLACK);
                ctx.y -= 22;

                ctx = drawMedsHeader(ctx, tableX, ctx.y, tableW, fontSize, wNo, wName, wDose, wFreq, wDur, wInstr);
                ctx.y -= 10;
            }

            float yTop = ctx.y;

            // Zebra bg
            if (zebra) {
                setFill(ctx.cs, Colors.GRAY_025);
                ctx.cs.addRect(tableX, yTop - rowH, tableW, rowH);
                ctx.cs.fill();
                setNonStroking(ctx.cs, Color.BLACK);
            }

            // Columns text
            float tx = tableX;
            float baseY = yTop - rowPadY - (fontSize * 0.85f);

            // No
            drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, baseY, no);
            tx += wNo;

            // Name multiline
            drawMultiline(ctx.cs, ctx.fontBold, fontSize, leading, tx + padX, baseY, nameLines);
            tx += wName;

            // Dose
            drawText(ctx.cs, ctx.font, fontSize, tx + padX, baseY, dose);
            tx += wDose;

            // Freq
            drawText(ctx.cs, ctx.font, fontSize, tx + padX, baseY, freq);
            tx += wFreq;

            // Dur
            drawText(ctx.cs, ctx.font, fontSize, tx + padX, baseY, dur);
            tx += wDur;

            // Instr multiline
            drawMultiline(ctx.cs, ctx.font, fontSize, leading, tx + padX, baseY, instrLines);

            // Horizontal rule
            setStroking(ctx.cs, Colors.GRAY_200);
            ctx.cs.moveTo(tableX, yTop - rowH);
            ctx.cs.lineTo(tableX + tableW, yTop - rowH);
            ctx.cs.stroke();

            // Vertical grid (pro)
            drawColumnGrid(ctx.cs, tableX, yTop, rowH, wNo, wName, wDose, wFreq, wDur, tableW);

            setStroking(ctx.cs, Color.BLACK);

            ctx.y = yTop - rowH;
            zebra = !zebra;
        }

        ctx.y -= 14;
        return ctx;
    }

    private PdfCtx drawMedsHeader(
            PdfCtx ctx,
            float x, float yTop,
            float tableW,
            float fontSize,
            float wNo, float wName, float wDose, float wFreq, float wDur, float wInstr
    ) throws IOException {

        final float headerH = 28f;
        final float padX = 8f;
        final float padTop = 7f;

        // Header bg
        setFill(ctx.cs, Colors.BLUE_700);
        ctx.cs.addRect(x, yTop - headerH, tableW, headerH);
        ctx.cs.fill();

        setNonStroking(ctx.cs, Color.WHITE);

        // ✅ baseline calculado (no fijo yTop - 15)
        float textY = yTop - padTop - fontSize;

        float tx = x;
        drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, textY, "#");
        tx += wNo;

        drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, textY, "Medicamento");
        tx += wName;

        drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, textY, "Dosis");
        tx += wDose;

        drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, textY, "Frecuencia");
        tx += wFreq;

        drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, textY, "Duración");
        tx += wDur;

        drawText(ctx.cs, ctx.fontBold, fontSize, tx + padX, textY, "Indicaciones");

        setNonStroking(ctx.cs, Color.BLACK);

        // Bottom rule
        setStroking(ctx.cs, Colors.BLUE_800);
        ctx.cs.moveTo(x, yTop - headerH);
        ctx.cs.lineTo(x + tableW, yTop - headerH);
        ctx.cs.stroke();
        setStroking(ctx.cs, Color.BLACK);

        // ✅ CLAVE: mueve el cursor debajo del header
        ctx.y = yTop - headerH;

        return ctx;
    }

    // ============================
    // SIGNATURE
    // ============================
    private PdfCtx drawSignatureBlock(PdfCtx ctx, HEADPrescription rx) throws IOException {
        ctx.ensureSpace(160);

        float x = ctx.m;
        float w = ctx.pageW();
        float yTop = ctx.y;

        // Title
        setNonStroking(ctx.cs, Colors.BLUE_700);
        drawText(ctx.cs, ctx.fontBold, 12f, x, yTop - 14, "Firma digital");
        setNonStroking(ctx.cs, Color.BLACK);

        ctx.y -= 24;

        // Signature box
        float boxH = 92f;
        float boxYTop = ctx.y;

        setFill(ctx.cs, Color.WHITE);
        ctx.cs.addRect(x, boxYTop - boxH, w, boxH);
        ctx.cs.fill();

        setStroking(ctx.cs, Colors.GRAY_200);
        ctx.cs.addRect(x, boxYTop - boxH, w, boxH);
        ctx.cs.stroke();
        setStroking(ctx.cs, Color.BLACK);

        // Render signature image (if exists)
        Optional<HEADSignatureVectorDto> vecOpt = parseSignature(rx.getSignatureVectorJson());
        if (vecOpt.isPresent() && vecOpt.get().strokes() != null && !vecOpt.get().strokes().isEmpty()) {
            BufferedImage img = HEADSignatureRenderer.renderToImage(vecOpt.get(), 1100, 320);
            PDImageXObject pdImg = LosslessFactory.createFromImage(ctx.doc, img);

            // Fit with padding, keep aspect
            float pad = 10f;
            float targetW = w - (pad * 2);
            float targetH = boxH - (pad * 2);

            // image is 1100x320
            float imgW = 1100f;
            float imgH = 320f;
            float scale = Math.min(targetW / imgW, targetH / imgH);

            float drawW = imgW * scale;
            float drawH = imgH * scale;

            float drawX = x + pad + ((targetW - drawW) / 2f);
            float drawY = (boxYTop - boxH) + pad + ((targetH - drawH) / 2f);

            ctx.cs.drawImage(pdImg, drawX, drawY, drawW, drawH);

        } else {
            setNonStroking(ctx.cs, new Color(120, 128, 140));
            drawText(ctx.cs, ctx.font, 10.5f, x + 12, (boxYTop - (boxH / 2f)), "— Sin firma —");
            setNonStroking(ctx.cs, Color.BLACK);
        }

        ctx.y = boxYTop - boxH - 14;

        // Signed meta
        String signedAt = (rx.getSignatureSignedAt() != null)
                ? formatInstant(rx.getSignatureSignedAt())
                : "—";

        String doctor = nullSafe(rx.getDoctorName());
        String license = nullSafe(rx.getDoctorLicenseNo());

        setNonStroking(ctx.cs, new Color(90, 98, 110));
        drawText(ctx.cs, ctx.font, 10f, x, ctx.y - 10, "Firmado por: " + doctor);
        drawText(ctx.cs, ctx.font, 10f, x, ctx.y - 24, "Cédula: " + license + "   •   Fecha firma: " + signedAt);
        setNonStroking(ctx.cs, Color.BLACK);

        ctx.y -= 36;
        return ctx;
    }

    // ============================
    // DOCTOR / PATIENT CARD HELPERS
    // ============================
    private PdfCtx drawCard(PdfCtx ctx, float x, float yTop, float w, float h, String title, List<Row> rows) throws IOException {
        // Card bg
        setFill(ctx.cs, Color.WHITE);
        ctx.cs.addRect(x, yTop - h, w, h);
        ctx.cs.fill();

        // Border
        setStroking(ctx.cs, Colors.GRAY_200);
        ctx.cs.addRect(x, yTop - h, w, h);
        ctx.cs.stroke();
        setStroking(ctx.cs, Color.BLACK);

        // Title strip
        setFill(ctx.cs, Colors.BLUE_050);
        ctx.cs.addRect(x, yTop - 20, w, 20);
        ctx.cs.fill();

        setNonStroking(ctx.cs, Colors.BLUE_700);
        drawText(ctx.cs, ctx.fontBold, 11.2f, x + 10, yTop - 14, title);
        setNonStroking(ctx.cs, Color.BLACK);

        float ty = yTop - 32;
        float labelSize = 9.2f;
        float valueSize = 10.4f;

        for (Row r : rows) {
            if (ty < (yTop - h + 10)) break;

            // label
            setNonStroking(ctx.cs, new Color(110, 118, 130));
            drawText(ctx.cs, ctx.font, labelSize, x + 10, ty, r.label);

            // value (wrap if too long)
            setNonStroking(ctx.cs, Color.BLACK);
            List<String> lines = wrapText(nullSafe(r.value), ctx.fontBold, valueSize, w - 20);
            lines = ellipsizeLines(lines, 2);

            float vy = ty - 12;
            drawMultiline(ctx.cs, ctx.fontBold, valueSize, 12.6f, x + 10, vy, lines);

            ty -= 26;
        }

        setNonStroking(ctx.cs, Color.BLACK);
        return ctx;
    }

    private static Row row(String label, Object value) {
        return new Row(label, value == null ? null : String.valueOf(value));
    }

    private static final class Row {
        final String label;
        final String value;
        Row(String label, String value) { this.label = label; this.value = value; }
    }

    // ============================
    // JSON SIGNATURE PARSER
    // ============================
    private Optional<HEADSignatureVectorDto> parseSignature(String json) {
        try {
            if (json == null || json.isBlank()) return Optional.empty();
            return Optional.of(om.readValue(json, HEADSignatureVectorDto.class));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    // ============================
    // PdfCtx (stateful renderer)
    // ============================
    private static final class PdfCtx {
        final PDDocument doc;
        PDPage page;
        PDPageContentStream cs;

        final float m;
        final float headerH;
        final float footerH;

        float y;

        final PDFont font;
        final PDFont fontBold;

        // For header
        final String clinicName;
        final String clinicAddr;
        final String doctorPhone;
        final String doctorEmail;

        private PdfCtx(PDDocument doc, PDPage page, PDPageContentStream cs,
                       float margin, float headerH, float footerH,
                       PDFont font, PDFont fontBold,
                       HEADPrescription rx) {
            this.doc = doc;
            this.page = page;
            this.cs = cs;
            this.m = margin;
            this.headerH = headerH;
            this.footerH = footerH;
            this.font = font;
            this.fontBold = fontBold;

            this.clinicName = nullSafe(rx.getDoctorClinicName());
            this.clinicAddr = nullSafe(rx.getDoctorClinicAddress());
            this.doctorPhone = nullSafe(rx.getDoctorPhone());
            this.doctorEmail = nullSafe(rx.getDoctorEmail());

            this.y = page.getMediaBox().getHeight() - margin - headerH;
        }

        static PdfCtx start(PDDocument doc, float margin, float headerH, float footerH,
                            PDFont font, PDFont fontBold,
                            HEADPrescription rx) throws IOException {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            PdfCtx ctx = new PdfCtx(doc, page, cs, margin, headerH, footerH, font, fontBold, rx);

            drawFixedHeader(ctx);
            return ctx;
        }

        float pageW() {
            return page.getMediaBox().getWidth() - (m * 2);
        }

        float pageH() {
            return page.getMediaBox().getHeight();
        }

        float bottomLimit() {
            return m + footerH;
        }

        void closeStream() throws IOException {
            if (cs != null) cs.close();
        }

        void ensureSpace(float needed) throws IOException {
            if ((y - needed) > bottomLimit()) return;
            newPage();
        }

        Space ensureSpaceWithNewPage(float needed) throws IOException {
            if ((y - needed) > bottomLimit()) return new Space(this, false);
            newPage();
            return new Space(this, true);
        }

        void newPage() throws IOException {
            // close old stream
            cs.close();

            // new page
            PDPage p = new PDPage(PDRectangle.LETTER);
            doc.addPage(p);
            PDPageContentStream ncs = new PDPageContentStream(doc, p);

            page = p;
            cs = ncs;

            drawFixedHeader(this);

            y = page.getMediaBox().getHeight() - m - headerH;
        }

        static final class Space {
            final PdfCtx ctx;
            final boolean newPage;
            Space(PdfCtx ctx, boolean newPage) { this.ctx = ctx; this.newPage = newPage; }
        }

        private static void drawFixedHeader(PdfCtx ctx) throws IOException {
            float x = ctx.m;
            float w = ctx.pageW();
            float topY = ctx.pageH() - ctx.m;

            // Top banner
            setFill(ctx.cs, Colors.BLUE_700);
            ctx.cs.addRect(x, topY - 58, w, 58);
            ctx.cs.fill();

            // Clinic name
            setNonStroking(ctx.cs, Color.WHITE);
            drawText(ctx.cs, ctx.fontBold, 13.2f, x + 14, topY - 22, safe(ctx.clinicName, "Salud Express"));

            // Clinic address
            drawText(ctx.cs, ctx.font, 9.6f, x + 14, topY - 36, safe(ctx.clinicAddr, " "));

            // Contact right
            float rx = x + w - 14;
            drawTextRight(ctx.cs, ctx.font, 9.6f, rx, topY - 22, safe(ctx.doctorPhone, " "));
            drawTextRight(ctx.cs, ctx.font, 9.6f, rx, topY - 36, safe(ctx.doctorEmail, " "));

            // Separator line under header
            setStroking(ctx.cs, Colors.GRAY_200);
            ctx.cs.moveTo(x, topY - 58);
            ctx.cs.lineTo(x + w, topY - 58);
            ctx.cs.stroke();

            setNonStroking(ctx.cs, Color.BLACK);
            setStroking(ctx.cs, Color.BLACK);
        }

        private static String safe(String s, String fallback) {
            if (s == null || s.isBlank() || "—".equals(s)) return fallback;
            return s;
        }
    }

    // ============================
    // SIGNATURE RENDERER (vector -> image)
    // ============================
    public static final class HEADSignatureRenderer {

        private HEADSignatureRenderer() {}

        public static BufferedImage renderToImage(HEADSignatureVectorDto v, int outW, int outH) {
            BufferedImage img = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();

            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                // transparent bg
                g.setComposite(AlphaComposite.Src);
                g.setColor(new Color(255, 255, 255, 0));
                g.fillRect(0, 0, outW, outH);

                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if (v == null || v.strokes() == null) return img;

                for (HEADSignatureStrokeDto stroke : v.strokes()) {
                    if (stroke == null || stroke.points() == null || stroke.points().isEmpty()) continue;

                    List<HEADSignaturePointDto> pts = stroke.points();

                    if (pts.size() == 1) {
                        HEADSignaturePointDto p = pts.get(0);
                        int x = Math.round(p.x() * outW);
                        int y = Math.round(p.y() * outH);
                        g.fillOval(x - 3, y - 3, 6, 6);
                        continue;
                    }

                    Path2D.Float path = new Path2D.Float();
                    HEADSignaturePointDto p0 = pts.get(0);
                    path.moveTo(p0.x() * outW, p0.y() * outH);

                    for (int i = 1; i < pts.size(); i++) {
                        HEADSignaturePointDto p = pts.get(i);
                        path.lineTo(p.x() * outW, p.y() * outH);
                    }
                    g.draw(path);
                }

                return img;
            } finally {
                g.dispose();
            }
        }
    }

    // ============================
    // DRAW HELPERS
    // ============================
    private static void drawColumnGrid(PDPageContentStream cs, float x, float yTop, float rowH,
                                       float wNo, float wName, float wDose, float wFreq, float wDur, float tableW) throws IOException {
        setStroking(cs, new Color(235, 238, 244));
        float xx = x + wNo;
        cs.moveTo(xx, yTop);
        cs.lineTo(xx, yTop - rowH);
        cs.stroke();

        xx += wName;
        cs.moveTo(xx, yTop);
        cs.lineTo(xx, yTop - rowH);
        cs.stroke();

        xx += wDose;
        cs.moveTo(xx, yTop);
        cs.lineTo(xx, yTop - rowH);
        cs.stroke();

        xx += wFreq;
        cs.moveTo(xx, yTop);
        cs.lineTo(xx, yTop - rowH);
        cs.stroke();

        xx += wDur;
        cs.moveTo(xx, yTop);
        cs.lineTo(xx, yTop - rowH);
        cs.stroke();

        setStroking(cs, Color.BLACK);
    }

    private static void drawText(PDPageContentStream cs, PDFont font, float size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : sanitize(text));
        cs.endText();
    }

    private static void drawTextRight(PDPageContentStream cs, PDFont font, float size, float rightX, float y, String text) throws IOException {
        String t = text == null ? "" : sanitize(text);
        float w = font.getStringWidth(t) / 1000f * size;
        drawText(cs, font, size, rightX - w, y, t);
    }

    private static void drawMultiline(PDPageContentStream cs, PDFont font, float size, float leading,
                                      float x, float y, List<String> lines) throws IOException {
        if (lines == null || lines.isEmpty()) return;
        float yy = y;
        for (String line : lines) {
            drawText(cs, font, size, x, yy, line);
            yy -= leading;
        }
    }

    private static void setFill(PDPageContentStream cs, Color c) throws IOException {
        cs.setNonStrokingColor(c);
    }

    private static void setNonStroking(PDPageContentStream cs, Color c) throws IOException {
        cs.setNonStrokingColor(c);
    }

    private static void setStroking(PDPageContentStream cs, Color c) throws IOException {
        cs.setStrokingColor(c);
    }

    private static String nullSafe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private static String nullSafe(Object o) {
        if (o == null) return "—";
        String s = String.valueOf(o);
        return (s.isBlank()) ? "—" : s;
    }

    // Avoid PDFBox showText crashing with control chars
    private static String sanitize(String s) {
        if (s == null) return "";
        // Keep it simple: replace newlines/tabs with spaces
        return s.replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .trim();
    }

    private static List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        if (text == null) return List.of();
        String t = sanitize(text);
        if (t.isEmpty() || "—".equals(t)) return List.of("—");

        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (String word : t.split("\\s+")) {
            if (word.isBlank()) continue;

            String cand = (line.length() == 0) ? word : (line + " " + word);
            float w = font.getStringWidth(cand) / 1000f * fontSize;

            if (w <= maxWidth) {
                line.setLength(0);
                line.append(cand);
            } else {
                if (line.length() > 0) lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }

        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private static List<String> ellipsizeLines(List<String> lines, int maxLines) {
        if (lines == null) return List.of();
        if (lines.size() <= maxLines) return lines;

        List<String> out = new ArrayList<>(lines.subList(0, maxLines));
        int last = out.size() - 1;
        out.set(last, out.get(last) + "…");
        return out;
    }

    private static String formatInstant(Instant instant) {
        try {
            return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.of("America/Mexico_City"))
                    .format(instant);
        } catch (Exception e) {
            return "—";
        }
    }

    // ============================
    // COLOR PALETTE
    // ============================
    private static final class Colors {
        static final Color BLUE_800 = new Color(12, 43, 88);
        static final Color BLUE_700 = new Color(18, 66, 120);
        static final Color BLUE_600 = new Color(26, 96, 170);
        static final Color BLUE_050 = new Color(235, 244, 255);

        static final Color GRAY_200 = new Color(220, 225, 232);
        static final Color GRAY_050 = new Color(246, 248, 251);
        static final Color GRAY_025 = new Color(250, 251, 253);
    }
}

package com.example.carrental.services;

import com.example.carrental.dto.FinancialReportDTO;
import com.example.carrental.model.Payment;
import com.example.carrental.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;

    @Value("${app.name:CarRental SaaS}")
    private String appName;

    @Value("${app.url:https://carrental.com}")
    private String appUrl;

    public byte[] generateInvoicePdf(FinancialReportDTO reportData) {
        try {
            Context context = new Context();
            context.setVariable("report", reportData);
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);
            context.setVariable("generatedAt", LocalDateTime.now());
            context.setVariable("invoiceDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            String htmlContent = templateEngine.process("pdf/invoice", context);

            // In production, use a library like Flying Saucer, iText, or wkhtmltopdf
            // For now, we'll simulate PDF generation
            return generatePdfFromHtml(htmlContent, "Invoice-" + reportData.getInvoiceNumber());

        } catch (Exception e) {
            log.error("Failed to generate invoice PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] generateReservationConfirmationPdf(Reservation reservation) {
        try {
            Context context = new Context();
            context.setVariable("reservation", reservation);
            context.setVariable("user", reservation.getUser());
            context.setVariable("vehicle", reservation.getVehicle());
            context.setVariable("appName", appName);
            context.setVariable("appUrl", appUrl);
            context.setVariable("generatedAt", LocalDateTime.now());

            String htmlContent = templateEngine.process("pdf/reservation-confirmation", context);

            return generatePdfFromHtml(htmlContent, "Reservation-" + reservation.getReservationCode());

        } catch (Exception e) {
            log.error("Failed to generate reservation confirmation PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] generatePaymentReceiptPdf(Payment payment) {
        try {
            Context context = new Context();
            context.setVariable("payment", payment);
            context.setVariable("reservation", payment.getReservation());
            context.setVariable("user", payment.getUser());
            context.setVariable("appName", appName);
            context.setVariable("generatedAt", LocalDateTime.now());

            String htmlContent = templateEngine.process("pdf/payment-receipt", context);

            return generatePdfFromHtml(htmlContent, "Receipt-" + payment.getPaymentCode());

        } catch (Exception e) {
            log.error("Failed to generate payment receipt PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] generateBusinessReportPdf(String reportTitle, Object reportData) {
        try {
            Context context = new Context();
            context.setVariable("reportTitle", reportTitle);
            context.setVariable("reportData", reportData);
            context.setVariable("appName", appName);
            context.setVariable("generatedAt", LocalDateTime.now());
            context.setVariable("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            String htmlContent = templateEngine.process("pdf/business-report", context);

            return generatePdfFromHtml(htmlContent, "Business-Report-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));

        } catch (Exception e) {
            log.error("Failed to generate business report PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] generateMonthlyStatementPdf(String userId, Object statementData) {
        try {
            Context context = new Context();
            context.setVariable("userId", userId);
            context.setVariable("statementData", statementData);
            context.setVariable("appName", appName);
            context.setVariable("generatedAt", LocalDateTime.now());
            context.setVariable("statementMonth", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));

            String htmlContent = templateEngine.process("pdf/monthly-statement", context);

            return generatePdfFromHtml(htmlContent, "Statement-" + userId + "-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")));

        } catch (Exception e) {
            log.error("Failed to generate monthly statement PDF", e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private byte[] generatePdfFromHtml(String htmlContent, String fileName) {
        try {
            // In production, use a proper PDF library like:
            // 1. Flying Saucer (org.xhtmlrenderer:flying-saucer-pdf-itext5)
            // 2. iText 7 (com.itextpdf:itext7-core)
            // 3. wkhtmltopdf wrapper

            // Example with Flying Saucer:
            // ITextRenderer renderer = new ITextRenderer();
            // renderer.setDocumentFromString(htmlContent);
            // renderer.layout();
            // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // renderer.createPDF(outputStream);
            // return outputStream.toByteArray();

            // For now, we'll simulate PDF generation by creating a placeholder
            String pdfContent = String.format(
                "PDF Document: %s\n\nGenerated at: %s\n\nHTML Content:\n%s",
                fileName,
                LocalDateTime.now(),
                htmlContent.substring(0, Math.min(htmlContent.length(), 500)) + "..."
            );

            log.info("Generated PDF: {} (simulated)", fileName);
            return pdfContent.getBytes();

        } catch (Exception e) {
            log.error("Failed to convert HTML to PDF", e);
            throw new RuntimeException("PDF conversion failed", e);
        }
    }

    public void savePdfToFile(byte[] pdfBytes, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfBytes);
            log.info("PDF saved to: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to save PDF to file: {}", filePath, e);
            throw new RuntimeException("PDF save failed", e);
        }
    }

    public String generatePdfFileName(String prefix, String identifier) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return String.format("%s-%s-%s.pdf", prefix, identifier, timestamp);
    }
}
package ec.ups.edu.proyectofinal.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ReportService {

    // Generar PDF de inscritos
    public byte[] generateRegistrationsPdf(Long eventId) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            document.add(new Paragraph("Reporte de Inscritos - Evento ID: " + eventId));
            document.add(new Paragraph("Generado bajo demanda."));
            // Aquí agregarías la lógica para iterar sobre las inscripciones del repositorio
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    // Generar Excel de inscritos
    public byte[] generateRegistrationsExcel(Long eventId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inscritos");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID Inscripción");
            headerRow.createCell(1).setCellValue("Participante");
            headerRow.createCell(2).setCellValue("Estado");

            // Iteración de datos reales aquí
            
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el Excel", e);
        }
    }

    // Generar Comprobante PDF (Participante)
    public byte[] generateCertificatePdf(Long registrationId) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            document.add(new Paragraph("Comprobante de Inscripción"));
            document.add(new Paragraph("ID de Registro: " + registrationId));
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante", e);
        }
    }
}

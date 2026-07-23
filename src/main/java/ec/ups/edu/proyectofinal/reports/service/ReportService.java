package ec.ups.edu.proyectofinal.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ec.ups.edu.proyectofinal.registrations.entity.Registration;
import ec.ups.edu.proyectofinal.registrations.repository.RegistrationRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final RegistrationRepository registrationRepository;

    // Inyectamos el repositorio para obtener los datos reales
    public ReportService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    // Generar PDF de inscritos
    public byte[] generateRegistrationsPdf(Long eventId) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Reporte de Inscritos - Evento ID: " + eventId));
            document.add(new Paragraph("Generado bajo demanda.\n\n"));

            // Obtenemos todos los inscritos del evento sin paginación
            List<Registration> registrations = registrationRepository.findByEvent_Id(eventId, Pageable.unpaged()).getContent();

            PdfPTable table = new PdfPTable(4);
            table.addCell("ID Inscripción");
            table.addCell("Participante");
            table.addCell("Email");
            table.addCell("Estado");

            for (Registration reg : registrations) {
                table.addCell(reg.getId().toString());
                table.addCell(reg.getParticipant().getFirstName() + " " + reg.getParticipant().getLastName());
                table.addCell(reg.getParticipant().getEmail());
                table.addCell(reg.getStatus());
            }

            document.add(table);
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
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Estado");
            headerRow.createCell(4).setCellValue("Fecha Registro");

            List<Registration> registrations = registrationRepository.findByEvent_Id(eventId, Pageable.unpaged()).getContent();

            int rowIdx = 1;
            for (Registration reg : registrations) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(reg.getId());
                row.createCell(1).setCellValue(reg.getParticipant().getFirstName() + " " + reg.getParticipant().getLastName());
                row.createCell(2).setCellValue(reg.getParticipant().getEmail());
                row.createCell(3).setCellValue(reg.getStatus());
                row.createCell(4).setCellValue(reg.getRegisteredAt().toString());
            }

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

            Registration reg = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

            document.add(new Paragraph("Comprobante de Inscripción Oficial"));
            document.add(new Paragraph("----------------------------------"));
            document.add(new Paragraph("ID de Registro: " + reg.getId()));
            document.add(new Paragraph("Código Único: " + reg.getRegistrationCode().toString()));
            document.add(new Paragraph("Participante: " + reg.getParticipant().getFirstName() + " " + reg.getParticipant().getLastName()));
            document.add(new Paragraph("Evento: " + reg.getEvent().getTitle()));
            document.add(new Paragraph("Estado de inscripción: " + reg.getStatus()));
            
            String confirmacion = reg.getConfirmedAt() != null ? reg.getConfirmedAt().toString() : "Pendiente";
            document.add(new Paragraph("Fecha de Confirmación: " + confirmacion));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante", e);
        }
    }
}

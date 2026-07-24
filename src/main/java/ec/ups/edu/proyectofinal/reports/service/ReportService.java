package ec.ups.edu.proyectofinal.reports.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ec.ups.edu.proyectofinal.events.entity.Event;
import ec.ups.edu.proyectofinal.events.repository.EventRepository;
import ec.ups.edu.proyectofinal.reports.dto.ReportStatisticsResponse;
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
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    // Inyectamos el repositorio para obtener los datos reales
    public ReportService(RegistrationRepository registrationRepository, EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
    }

    public ReportStatisticsResponse generateSystemStatistics(Instant from, Instant to) {
        DateRange range = normalizeRange(from, to);

        return new ReportStatisticsResponse(
                null,
                range.from(),
                range.to(),
                Instant.now(),
                eventRepository.countByDeletedFalseAndCreatedAtBetween(range.from(), range.to()),
                eventRepository.countByStatusAndDeletedFalseAndCreatedAtBetween("PUBLISHED", range.from(), range.to()),
                registrationRepository.countByRegisteredAtBetween(range.from(), range.to()),
                registrationRepository.countByStatusAndRegisteredAtBetween("PENDING", range.from(), range.to()),
                registrationRepository.countByStatusAndRegisteredAtBetween("CONFIRMED", range.from(), range.to()),
                registrationRepository.countByStatusAndRegisteredAtBetween("CANCELLED", range.from(), range.to()),
                registrationRepository.countByStatusAndRegisteredAtBetween("REJECTED", range.from(), range.to())
        );
    }

    public ReportStatisticsResponse generateEventStatistics(Long eventId, Instant from, Instant to) {
        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        DateRange range = normalizeRange(from, to);

        return new ReportStatisticsResponse(
                event.getId(),
                range.from(),
                range.to(),
                Instant.now(),
                1,
                "PUBLISHED".equals(event.getStatus()) ? 1 : 0,
                registrationRepository.countByEvent_IdAndRegisteredAtBetween(eventId, range.from(), range.to()),
                registrationRepository.countByEvent_IdAndStatusAndRegisteredAtBetween(eventId, "PENDING", range.from(), range.to()),
                registrationRepository.countByEvent_IdAndStatusAndRegisteredAtBetween(eventId, "CONFIRMED", range.from(), range.to()),
                registrationRepository.countByEvent_IdAndStatusAndRegisteredAtBetween(eventId, "CANCELLED", range.from(), range.to()),
                registrationRepository.countByEvent_IdAndStatusAndRegisteredAtBetween(eventId, "REJECTED", range.from(), range.to())
        );
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
        } catch (DocumentException | IOException e) {
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
        Registration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        if (!"CONFIRMED".equals(reg.getStatus())) {
            throw new RuntimeException("Solo se puede generar el comprobante de una inscripcion confirmada");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

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
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error al generar el comprobante", e);
        }
    }

    private DateRange normalizeRange(Instant from, Instant to) {
        Instant normalizedTo = to == null ? Instant.now() : to;
        Instant normalizedFrom = from == null ? Instant.EPOCH : from;

        if (normalizedFrom.isAfter(normalizedTo)) {
            throw new RuntimeException("El rango de fechas no es valido");
        }

        return new DateRange(normalizedFrom, normalizedTo);
    }

    private record DateRange(Instant from, Instant to) {
    }
}
